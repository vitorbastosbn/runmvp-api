package com.runmvp.worker.subscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SubscriptionReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionReconciliationJob.class);
    private static final String PLAY_API_URL =
        "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/%s/purchases/subscriptionsv2/tokens/%s";
    private static final Set<String> ENTITLED_STATES = Set.of(
        "SUBSCRIPTION_STATE_ACTIVE",
        "SUBSCRIPTION_STATE_CANCELED",
        "SUBSCRIPTION_STATE_IN_GRACE_PERIOD"
    );

    @Value("${google.play.package-name}")
    private String packageName;

    @Value("${google.play.credentials-path:}")
    private String credentialsPath;

    private final JdbcTemplate jdbc;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public SubscriptionReconciliationJob(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${worker.subscription.reconciliation-interval-ms:21600000}",
               initialDelayString = "60000")
    @Transactional
    public void reconcile() {
        List<Map<String, Object>> active = jdbc.queryForList("""
            SELECT id, purchase_token, product_id
            FROM subscriptions
            WHERE status IN ('ACTIVE', 'CANCELLED')
              AND expires_at > now() - interval '7 days'
            """);

        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (IOException e) {
            log.error("Cannot get Google Play access token: {}", e.getMessage());
            return;
        }

        for (Map<String, Object> row : active) {
            try {
                reconcileOne(
                    ((Number) row.get("id")).longValue(),
                    (String) row.get("purchase_token"),
                    (String) row.get("product_id"),
                    accessToken
                );
            } catch (Exception e) {
                log.warn("Reconciliation failed for subscription {}: {}",
                    row.get("id"), e.getMessage());
            }
        }
    }

    private void reconcileOne(Long id, String purchaseToken, String productId,
                               String accessToken) throws Exception {
        String url = PLAY_API_URL.formatted(packageName, purchaseToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        JsonNode root = mapper.readTree(response.getBody());
        String subscriptionState = root.path("subscriptionState")
            .asText("SUBSCRIPTION_STATE_UNSPECIFIED");

        JsonNode lineItems = root.path("lineItems");
        if (!lineItems.isArray() || lineItems.isEmpty()) return;

        String expiryStr = lineItems.get(0).path("expiryTime").asText(null);
        if (expiryStr == null) return;
        Instant expiryTime = Instant.parse(expiryStr);

        if (ENTITLED_STATES.contains(subscriptionState)) {
            int updated = jdbc.update("""
                UPDATE subscriptions
                SET expires_at = ?, status = 'ACTIVE', updated_at = now()
                WHERE id = ? AND expires_at < ?
                """, expiryTime, id, expiryTime);
            if (updated > 0) log.info("Reconciled renewal for subscription {}: new expiry {}",
                id, expiryTime);
        } else if ("SUBSCRIPTION_STATE_EXPIRED".equals(subscriptionState)) {
            jdbc.update("""
                UPDATE subscriptions SET status = 'EXPIRED', updated_at = now()
                WHERE id = ? AND status != 'EXPIRED'
                """, id);
        }
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials credentials;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of("https://www.googleapis.com/auth/androidpublisher"));
        } else {
            credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of("https://www.googleapis.com/auth/androidpublisher"));
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
