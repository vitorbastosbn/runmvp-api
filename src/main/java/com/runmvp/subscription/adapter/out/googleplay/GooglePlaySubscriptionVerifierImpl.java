package com.runmvp.subscription.adapter.out.googleplay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.runmvp.subscription.application.port.out.GooglePlaySubscriptionVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class GooglePlaySubscriptionVerifierImpl implements GooglePlaySubscriptionVerifier {

    private static final String PLAY_API_URL =
        "https://androidpublisher.googleapis.com/androidpublisher/v3/applications/%s/purchases/subscriptionsv2/tokens/%s";

    @Value("${google.play.package-name}")
    private String packageName;

    @Value("${google.play.credentials-path:}")
    private String credentialsPath;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public VerifiedSubscription verify(String purchaseToken, String productId) {
        try {
            String accessToken = getAccessToken();
            String url = PLAY_API_URL.formatted(packageName, purchaseToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            JsonNode root = mapper.readTree(response.getBody());
            String subscriptionState = root.path("subscriptionState")
                .asText("SUBSCRIPTION_STATE_UNSPECIFIED");

            JsonNode lineItems = root.path("lineItems");
            Instant expiryTime = Instant.now();
            if (lineItems.isArray() && !lineItems.isEmpty()) {
                String expiryStr = lineItems.get(0).path("expiryTime").asText(null);
                if (expiryStr != null) expiryTime = Instant.parse(expiryStr);
            }

            return new VerifiedSubscription(subscriptionState, productId, expiryTime);

        } catch (IOException e) {
            throw new RuntimeException("Google Play verification failed: " + e.getMessage(), e);
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
