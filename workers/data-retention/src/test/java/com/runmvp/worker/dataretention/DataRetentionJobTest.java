package com.runmvp.worker.dataretention;

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
class DataRetentionJobTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired JdbcTemplate jdbc;
    @Autowired DataRetentionJob job;

    @Test
    void incrementalClean_removesOldProcessedOutboxEvents() {
        jdbc.update("""
            INSERT INTO outbox_events
              (aggregate_type, aggregate_id, event_type, payload, created_at, processed_at)
            VALUES ('RunningSession', 1, 'test.event', '{}',
                    now() - interval '8 days', now() - interval '8 days')
            """);

        jdbc.update("""
            INSERT INTO outbox_events
              (aggregate_type, aggregate_id, event_type, payload, created_at, processed_at)
            VALUES ('RunningSession', 2, 'test.event', '{}',
                    now() - interval '1 day', now() - interval '1 day')
            """);

        job.incrementalClean();

        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM outbox_events WHERE aggregate_id IN (1, 2)",
            Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void incrementalClean_doesNotDeleteUnprocessedEvents() {
        jdbc.update("""
            INSERT INTO outbox_events
              (aggregate_type, aggregate_id, event_type, payload, created_at)
            VALUES ('RunningSession', 99, 'test.unprocessed', '{}', now() - interval '30 days')
            """);

        job.incrementalClean();

        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM outbox_events WHERE aggregate_id = 99",
            Integer.class);
        assertThat(count).isEqualTo(1);
    }
}
