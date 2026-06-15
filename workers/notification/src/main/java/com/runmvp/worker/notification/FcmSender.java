package com.runmvp.worker.notification;

import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class FcmSender {

    private static final Logger log = LoggerFactory.getLogger(FcmSender.class);
    private static final String FCM_URL =
        "https://fcm.googleapis.com/v1/projects/%s/messages:send";

    @Value("${fcm.project-id}")
    private String projectId;

    @Value("${fcm.credentials-path:}")
    private String credentialsPath;

    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            String accessToken = getAccessToken();
            String url = FCM_URL.formatted(projectId);

            Map<String, Object> message = Map.of(
                "message", Map.of(
                    "token", fcmToken,
                    "notification", Map.of("title", title, "body", body),
                    "data", data
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(message, headers), String.class);
        } catch (Exception e) {
            log.warn("FCM send failed for token {}: {}", fcmToken, e.getMessage());
        }
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials credentials;
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        } else {
            credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        }
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
