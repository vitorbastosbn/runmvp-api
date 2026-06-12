package com.runmvp.shared.entitlement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class EntitlementAdapter implements EntitlementPort {

    private final JdbcTemplate jdbc;

    EntitlementAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Entitlement getEffectiveEntitlement(Long userId) {
        String sql = """
            SELECT COUNT(*) FROM subscriptions
            WHERE user_id = ?
              AND expires_at > now()
              AND (status IN ('ACTIVE', 'GRACE_PERIOD')
               OR (status = 'CANCELLED' AND expires_at > now()))
            """;
        Integer count = jdbc.queryForObject(sql, Integer.class, userId);
        return (count != null && count > 0) ? Entitlement.PREMIUM_ACTIVE : Entitlement.FREE;
    }
}
