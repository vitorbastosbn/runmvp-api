package com.runmvp.worker.sessionfinalizer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
class SessionFinalizerJobTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired JdbcTemplate jdbc;
    @Autowired SessionFinalizerJob job;

    @Test
    void processAllFinished_marksSessionCompleted() {
        jdbc.update("INSERT INTO users (google_subject, name, email, public_code) VALUES (?,?,?,?)",
            "sub1", "Alice", "a@x.com", "ALICE001");
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE google_subject='sub1'", Long.class);

        jdbc.update("""
            INSERT INTO running_sessions (creator_id, status, mode, created_at, updated_at)
            VALUES (?, 'ACTIVE', 'COMPETITIVE', now(), now())
            """, userId);
        Long sessionId = jdbc.queryForObject("SELECT MAX(id) FROM running_sessions", Long.class);

        jdbc.update("""
            INSERT INTO session_participants
              (session_id, user_id, status, role, official_distance_meters,
               running_time_seconds, created_at, updated_at)
            VALUES (?, ?, 'FINISHED', 'CREATOR', 5000, 1200, now(), now())
            """, sessionId, userId);

        jdbc.update("""
            INSERT INTO outbox_events
              (aggregate_type, aggregate_id, event_type, payload, created_at)
            VALUES ('RunningSession', ?, 'session.all_finished', '{}', now())
            """, sessionId);

        job.process();

        String status = jdbc.queryForObject(
            "SELECT status FROM running_sessions WHERE id = ?", String.class, sessionId);
        assertThat(status).isEqualTo("COMPLETED");
    }
}
