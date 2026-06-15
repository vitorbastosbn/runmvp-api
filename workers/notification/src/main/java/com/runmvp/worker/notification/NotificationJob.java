package com.runmvp.worker.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class NotificationJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationJob.class);

    private final JdbcTemplate jdbc;
    private final FcmSender fcmSender;

    public NotificationJob(JdbcTemplate jdbc, FcmSender fcmSender) {
        this.jdbc = jdbc;
        this.fcmSender = fcmSender;
    }

    @Scheduled(fixedDelayString = "${worker.notification.poll-interval-ms:5000}")
    @Transactional
    public void scheduledProcess() { process(); }

    public void process() {
        List<Map<String, Object>> events = jdbc.queryForList(
            """
            SELECT id, aggregate_id, event_type, payload
            FROM outbox_events
            WHERE event_type IN (
              'session_invite_sent', 'session.cancelled',
              'session.completed', 'participant.finished'
            )
              AND processed_at IS NULL
            ORDER BY created_at
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """
        );

        for (Map<String, Object> event : events) {
            try {
                dispatch(event);
                jdbc.update(
                    "UPDATE outbox_events SET processed_at = now() WHERE id = ?",
                    event.get("id"));
            } catch (Exception e) {
                log.error("Notification dispatch failed for event {}: {}",
                    event.get("id"), e.getMessage());
            }
        }
    }

    private void dispatch(Map<String, Object> event) {
        String eventType = (String) event.get("event_type");
        Long sessionId = ((Number) event.get("aggregate_id")).longValue();

        List<String> tokens = jdbc.queryForList(
            """
            SELECT dt.fcm_token
            FROM device_tokens dt
            JOIN session_participants sp ON sp.user_id = dt.user_id
            WHERE sp.session_id = ?
              AND sp.status NOT IN ('DECLINED', 'ABANDONED')
            """,
            String.class, sessionId
        );

        if (tokens.isEmpty()) return;

        String title = switch (eventType) {
            case "session.cancelled"    -> "Run cancelled";
            case "session.completed"    -> "Run finished!";
            case "participant.finished" -> "Opponent finished";
            case "session_invite_sent"  -> "New run invite";
            default -> eventType;
        };
        String body = switch (eventType) {
            case "session.cancelled"    -> "The session was cancelled.";
            case "session.completed"    -> "Check your results!";
            case "participant.finished" -> "A runner has crossed the finish line.";
            case "session_invite_sent"  -> "You've been invited to run!";
            default -> "";
        };

        Map<String, String> data = Map.of(
            "type", "session_invite_sent".equals(eventType) ? "session_invite" : "session_started",
            "sessionId", String.valueOf(sessionId)
        );

        for (String token : tokens) {
            fcmSender.send(token, title, body, data);
        }
    }
}
