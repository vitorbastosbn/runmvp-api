package com.runmvp.worker.statistics;

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
class StatisticsJobTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired JdbcTemplate jdbc;
    @Autowired StatisticsJob job;

    @Test
    void processCompletedSession_updatesUserStatistics() {
        jdbc.update("INSERT INTO users (google_subject, name, email, public_code) VALUES (?,?,?,?)",
            "sub-stats", "Bob", "b@x.com", "BOB00001");
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE google_subject='sub-stats'", Long.class);

        jdbc.update("""
            INSERT INTO running_sessions
              (creator_id, status, mode, started_at, finished_at, created_at, updated_at)
            VALUES (?, 'COMPLETED', 'COMPETITIVE', now()-interval'20 minutes', now(), now()-interval'21 minutes', now())
            """, userId);
        Long sessionId = jdbc.queryForObject("SELECT MAX(id) FROM running_sessions", Long.class);

        jdbc.update("""
            INSERT INTO session_participants
              (session_id, user_id, status, role, final_position,
               official_distance_meters, running_time_seconds, created_at, updated_at)
            VALUES (?, ?, 'FINISHED', 'CREATOR', 1, 5000, 1200, now(), now())
            """, sessionId, userId);

        jdbc.update("""
            INSERT INTO outbox_events
              (aggregate_type, aggregate_id, event_type, payload, created_at)
            VALUES ('RunningSession', ?, 'session.completed', '{}', now())
            """, sessionId);

        job.process();

        Integer totalSessions = jdbc.queryForObject(
            "SELECT total_sessions FROM user_statistics WHERE user_id = ?",
            Integer.class, userId);
        assertThat(totalSessions).isEqualTo(1);

        Integer podiumHistoryCount = jdbc.queryForObject(
            "SELECT podium_history_count FROM competitive_statistics WHERE user_id = ?",
            Integer.class, userId);
        assertThat(podiumHistoryCount).isEqualTo(1);
    }
}
