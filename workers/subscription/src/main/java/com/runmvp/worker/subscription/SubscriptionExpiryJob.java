package com.runmvp.worker.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SubscriptionExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryJob.class);

    private final JdbcTemplate jdbc;

    public SubscriptionExpiryJob(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${worker.subscription.expiry-poll-ms:10000}")
    @Transactional
    public void scheduledProcess() { process(); }

    public void process() {
        int updated = jdbc.update("""
            UPDATE subscriptions
            SET status = 'EXPIRED', updated_at = now()
            WHERE status IN ('ACTIVE', 'CANCELLED')
              AND expires_at <= now()
            """);

        if (updated > 0) {
            log.info("Marked {} subscriptions as EXPIRED", updated);
        }
    }
}
