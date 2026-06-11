package com.runmvp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationIT extends BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void allMigrationsApplySuccessfully() throws Exception {
        String[] expectedTables = {
            "users", "refresh_tokens", "device_tokens",
            "friendships",
            "running_sessions", "session_participants", "location_points",
            "user_statistics", "competitive_statistics",
            "subscriptions", "play_billing_events", "ad_profiles",
            "outbox_events"
        };
        try (Connection conn = dataSource.getConnection()) {
            for (String table : expectedTables) {
                ResultSet rs = conn.getMetaData().getTables(null, "public", table, new String[]{"TABLE"});
                assertThat(rs.next())
                    .as("Table '%s' should exist after migrations", table)
                    .isTrue();
            }
        }
    }
}
