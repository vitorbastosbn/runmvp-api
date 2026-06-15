package com.runmvp.worker.subscription;

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
class SubscriptionExpiryJobTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired JdbcTemplate jdbc;
    @Autowired SubscriptionExpiryJob job;

    @Test
    void process_expiredActiveSubscription_marksExpired() {
        jdbc.update("INSERT INTO users (google_subject, name, email, public_code) VALUES (?,?,?,?)",
            "sub-sub", "Sub User", "s@x.com", "SUB00001");
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE google_subject='sub-sub'", Long.class);

        jdbc.update("""
            INSERT INTO subscriptions
              (user_id, purchase_token, product_id, product_type, status, entitlement,
               started_at, expires_at, created_at, updated_at)
            VALUES (?, 'token-exp', 'monthly_premium', 'MONTHLY', 'ACTIVE', 'PREMIUM_ACTIVE',
                    now() - interval '31 days', now() - interval '1 hour', now(), now())
            """, userId);

        job.process();

        String status = jdbc.queryForObject(
            "SELECT status FROM subscriptions WHERE purchase_token = 'token-exp'",
            String.class);
        assertThat(status).isEqualTo("EXPIRED");
    }

    @Test
    void process_activeNotExpired_doesNotChange() {
        jdbc.update("INSERT INTO users (google_subject, name, email, public_code) VALUES (?,?,?,?)",
            "sub-active", "Active User", "active@x.com", "ACTV0001");
        Long userId = jdbc.queryForObject("SELECT id FROM users WHERE google_subject='sub-active'", Long.class);

        jdbc.update("""
            INSERT INTO subscriptions
              (user_id, purchase_token, product_id, product_type, status, entitlement,
               started_at, expires_at, created_at, updated_at)
            VALUES (?, 'token-ok', 'monthly_premium', 'MONTHLY', 'ACTIVE', 'PREMIUM_ACTIVE',
                    now(), now() + interval '30 days', now(), now())
            """, userId);

        job.process();

        String status = jdbc.queryForObject(
            "SELECT status FROM subscriptions WHERE purchase_token = 'token-ok'",
            String.class);
        assertThat(status).isEqualTo("ACTIVE");
    }
}
