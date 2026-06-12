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
            SELECT entitlement FROM subscriptions
            WHERE user_id = ?
              AND (status IN ('ACTIVE', 'GRACE_PERIOD')
               OR (status = 'CANCELLED' AND expires_at > now()))
            ORDER BY updated_at DESC
            LIMIT 1
            """;
        return jdbc.query(sql, rs -> {
            if (rs.next()) {
                String val = rs.getString("entitlement");
                return "PREMIUM_ACTIVE".equals(val)
                    ? Entitlement.PREMIUM_ACTIVE
                    : Entitlement.FREE;
            }
            return Entitlement.FREE;
        }, userId);
    }
}
