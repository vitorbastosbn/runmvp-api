package com.runmvp.worker.sessionfinalizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SessionFinalizerJob {

    private static final Logger log = LoggerFactory.getLogger(SessionFinalizerJob.class);

    private final JdbcTemplate jdbc;

    public SessionFinalizerJob(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(fixedDelayString = "${worker.session-finalizer.poll-interval-ms:15000}")
    @Transactional
    public void scheduledProcess() {
        process();
    }

    public void process() {
        List<Long> sessionIds = jdbc.queryForList(
            """
            SELECT DISTINCT aggregate_id FROM (
                SELECT aggregate_id
                FROM outbox_events
                WHERE event_type = 'session.all_finished'
                  AND processed_at IS NULL
                ORDER BY created_at
                LIMIT 50
                FOR UPDATE SKIP LOCKED
            ) sub
            """,
            Long.class
        );

        for (Long sessionId : sessionIds) {
            try {
                finalizeSession(sessionId);
            } catch (Exception e) {
                log.error("Failed to finalize session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    private void finalizeSession(Long sessionId) {
        Integer activeCount = jdbc.queryForObject(
            """
            SELECT COUNT(*) FROM session_participants
            WHERE session_id = ?
              AND status NOT IN ('FINISHED', 'ABANDONED', 'DECLINED')
            """,
            Integer.class, sessionId
        );

        if (activeCount != null && activeCount > 0) {
            log.warn("Session {} still has {} active participants, skipping", sessionId, activeCount);
            return;
        }

        jdbc.update(
            "UPDATE running_sessions SET status = 'COMPLETED', finished_at = now(), updated_at = now() WHERE id = ?",
            sessionId
        );

        jdbc.update(
            """
            INSERT INTO outbox_events
              (aggregate_type, aggregate_id, event_type, payload, created_at)
            VALUES ('RunningSession', ?, 'session.completed', ?::jsonb, now())
            """,
            sessionId,
            String.format("{\"sessionId\": %d}", sessionId)
        );

        jdbc.update(
            """
            UPDATE outbox_events SET processed_at = now()
            WHERE aggregate_id = ? AND event_type = 'session.all_finished'
              AND processed_at IS NULL
            """,
            sessionId
        );

        log.info("Session {} finalized as COMPLETED", sessionId);
    }
}
