package com.runmvp.worker.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class NotificationJobTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired JdbcTemplate jdbc;
    @Autowired NotificationJob job;
    @MockBean FcmSender fcmSender;

    @Test
    void process_sessionCancelledEvent_sendsToParticipantTokens() {
        jdbc.update("INSERT INTO users (google_subject, name, email, public_code) VALUES (?,?,?,?)",
            "sub-notif", "Notif User", "n@x.com", "NOTIF001");
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE google_subject='sub-notif'", Long.class);

        jdbc.update("INSERT INTO device_tokens (user_id, fcm_token) VALUES (?, 'device-fcm-1')", userId);

        jdbc.update("INSERT INTO running_sessions (creator_id, status, mode, created_at, updated_at) VALUES (?, 'ABANDONED', 'COMPETITIVE', now(), now())", userId);
        Long sessionId = jdbc.queryForObject("SELECT MAX(id) FROM running_sessions", Long.class);

        jdbc.update("INSERT INTO session_participants (session_id, user_id, status, role, created_at, updated_at) VALUES (?, ?, 'ACCEPTED', 'CREATOR', now(), now())",
            sessionId, userId);

        jdbc.update("INSERT INTO outbox_events (aggregate_type, aggregate_id, event_type, payload, created_at) VALUES ('RunningSession', ?, 'session.cancelled', '{}', now())",
            sessionId);

        job.process();

        verify(fcmSender).send(eq("device-fcm-1"), eq("Run cancelled"), anyString(), anyMap());
    }
}
