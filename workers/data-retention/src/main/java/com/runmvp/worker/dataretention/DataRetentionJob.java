package com.runmvp.worker.dataretention;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataRetentionJob {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionJob.class);

    private final JdbcTemplate jdbc;

    @Value("${worker.data-retention.outbox-retention-days:7}")
    private int outboxRetentionDays;

    @Value("${worker.data-retention.location-points-retention-days:90}")
    private int locationPointsRetentionDays;

    @Value("${worker.data-retention.billing-events-retention-days:180}")
    private int billingEventsRetentionDays;

    public DataRetentionJob(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${worker.data-retention.incremental-poll-ms:60000}")
    @Transactional
    public void scheduledIncrementalClean() { incrementalClean(); }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void scheduledDeepClean() { deepClean(); }

    @Transactional
    public void incrementalClean() {
        int deleted = jdbc.update(
            "DELETE FROM outbox_events WHERE processed_at IS NOT NULL AND processed_at < now() - (? || ' days')::interval",
            String.valueOf(outboxRetentionDays)
        );
        if (deleted > 0) log.info("Deleted {} processed outbox events", deleted);
    }

    @Transactional
    public void deepClean() {
        int locationDeleted = jdbc.update("""
            DELETE FROM location_points lp
            USING session_participants sp
            JOIN running_sessions rs ON rs.id = sp.session_id
            WHERE sp.id = lp.participant_id
              AND rs.finished_at < now() - (? || ' days')::interval
              AND rs.status IN ('COMPLETED', 'ABANDONED')
            """,
            String.valueOf(locationPointsRetentionDays)
        );
        if (locationDeleted > 0) log.info("Deleted {} old location_points", locationDeleted);

        int billingDeleted = jdbc.update(
            "DELETE FROM play_billing_events WHERE received_at < now() - (? || ' days')::interval",
            String.valueOf(billingEventsRetentionDays)
        );
        if (billingDeleted > 0) log.info("Deleted {} old play_billing_events", billingDeleted);

        int tokenDeleted = jdbc.update("""
            DELETE FROM device_tokens dt
            USING users u
            WHERE u.id = dt.user_id AND u.deleted_at IS NOT NULL
            """);
        if (tokenDeleted > 0) log.info("Deleted {} device_tokens of deleted users", tokenDeleted);

        int refreshDeleted = jdbc.update("""
            DELETE FROM refresh_tokens rt
            USING users u
            WHERE u.id = rt.user_id AND u.deleted_at IS NOT NULL
            """);
        if (refreshDeleted > 0) log.info("Deleted {} refresh_tokens of deleted users", refreshDeleted);
    }
}
