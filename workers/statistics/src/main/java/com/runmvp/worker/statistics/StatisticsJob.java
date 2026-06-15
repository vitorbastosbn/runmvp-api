package com.runmvp.worker.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class StatisticsJob {

    private static final Logger log = LoggerFactory.getLogger(StatisticsJob.class);

    private final JdbcTemplate jdbc;

    public StatisticsJob(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${worker.statistics.poll-interval-ms:10000}")
    @Transactional
    public void scheduledProcess() { process(); }

    public void process() {
        List<Long> sessionIds = jdbc.queryForList(
            """
            SELECT DISTINCT aggregate_id FROM (
                SELECT aggregate_id
                FROM outbox_events
                WHERE event_type = 'session.completed'
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
                aggregateStats(sessionId);
                jdbc.update(
                    "UPDATE outbox_events SET processed_at = now() WHERE aggregate_id = ? AND event_type = 'session.completed' AND processed_at IS NULL",
                    sessionId);
            } catch (Exception e) {
                log.error("Failed to aggregate stats for session {}: {}", sessionId, e.getMessage());
            }
        }
    }

    private void aggregateStats(Long sessionId) {
        String mode = jdbc.queryForObject(
            "SELECT mode FROM running_sessions WHERE id = ?", String.class, sessionId);

        List<Map<String, Object>> participants = jdbc.queryForList(
            """
            SELECT user_id, final_position, official_distance_meters, running_time_seconds
            FROM session_participants
            WHERE session_id = ? AND status = 'FINISHED'
            """, sessionId);

        for (Map<String, Object> p : participants) {
            Long userId = ((Number) p.get("user_id")).longValue();
            long distance = p.get("official_distance_meters") != null
                ? ((Number) p.get("official_distance_meters")).longValue() : 0L;
            long time = p.get("running_time_seconds") != null
                ? ((Number) p.get("running_time_seconds")).longValue() : 0L;
            int position = p.get("final_position") != null
                ? ((Number) p.get("final_position")).intValue() : 0;

            boolean isPodium = position > 0 && position <= 3;
            boolean isFirst  = position == 1;
            int coopIncrement = "COOPERATIVE".equals(mode) ? 1 : 0;

            jdbc.update("""
                INSERT INTO user_statistics
                  (user_id, total_sessions, completed_sessions, official_distance_meters,
                   total_running_time_seconds, longest_distance_meters,
                   podiums, first_places, completed_cooperative_sessions, updated_at)
                VALUES (?, 1, 1, ?, ?, ?, ?, ?, ?, now())
                ON CONFLICT (user_id) DO UPDATE SET
                  total_sessions = user_statistics.total_sessions + 1,
                  completed_sessions = user_statistics.completed_sessions + 1,
                  official_distance_meters = user_statistics.official_distance_meters + EXCLUDED.official_distance_meters,
                  total_running_time_seconds = user_statistics.total_running_time_seconds + EXCLUDED.total_running_time_seconds,
                  longest_distance_meters = GREATEST(user_statistics.longest_distance_meters, EXCLUDED.longest_distance_meters),
                  podiums = user_statistics.podiums + EXCLUDED.podiums,
                  first_places = user_statistics.first_places + EXCLUDED.first_places,
                  completed_cooperative_sessions = user_statistics.completed_cooperative_sessions + EXCLUDED.completed_cooperative_sessions,
                  updated_at = now()
                """,
                userId, distance, time, distance,
                isPodium ? 1 : 0, isFirst ? 1 : 0, coopIncrement
            );

            if ("COMPETITIVE".equals(mode)) {
                jdbc.update("""
                    INSERT INTO competitive_statistics
                      (user_id, podium_history_count, first_place_history_count, updated_at)
                    VALUES (?, ?, ?, now())
                    ON CONFLICT (user_id) DO UPDATE SET
                      podium_history_count = competitive_statistics.podium_history_count + EXCLUDED.podium_history_count,
                      first_place_history_count = competitive_statistics.first_place_history_count + EXCLUDED.first_place_history_count,
                      updated_at = now()
                    """,
                    userId, isPodium ? 1 : 0, isFirst ? 1 : 0
                );
            }
        }
    }
}
