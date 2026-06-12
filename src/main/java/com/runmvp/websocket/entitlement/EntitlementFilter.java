package com.runmvp.websocket.entitlement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runmvp.shared.entitlement.EntitlementPort;
import org.springframework.stereotype.Component;

@Component
public class EntitlementFilter {

    private final EntitlementPort entitlementPort;

    public EntitlementFilter(EntitlementPort entitlementPort) {
        this.entitlementPort = entitlementPort;
    }

    public JsonNode filterPayload(Long userId, String eventType, JsonNode originalPayload) {
        boolean isPremium = entitlementPort.getEffectiveEntitlement(userId) ==
            EntitlementPort.Entitlement.PREMIUM_ACTIVE;

        if (!isPremium && "session.participant_update".equals(eventType)) {
            ObjectNode filtered = ((ObjectNode) originalPayload.deepCopy());
            filtered.remove("ranking");
            return filtered;
        }
        return originalPayload;
    }
}
