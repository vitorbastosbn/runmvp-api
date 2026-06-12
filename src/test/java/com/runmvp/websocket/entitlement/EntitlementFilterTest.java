package com.runmvp.websocket.entitlement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runmvp.shared.entitlement.EntitlementPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntitlementFilterTest {

    @Mock EntitlementPort entitlementPort;
    @InjectMocks EntitlementFilter filter;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void filterPayload_freeUser_participantUpdate_removesRanking() {
        when(entitlementPort.getEffectiveEntitlement(1L)).thenReturn(EntitlementPort.Entitlement.FREE);

        ObjectNode payload = mapper.createObjectNode();
        payload.put("position", 1);
        payload.putArray("ranking");

        var result = filter.filterPayload(1L, "session.participant_update", payload);

        assertThat(result.has("ranking")).isFalse();
        assertThat(result.has("position")).isTrue();
    }

    @Test
    void filterPayload_premiumUser_participantUpdate_keepsRanking() {
        when(entitlementPort.getEffectiveEntitlement(1L)).thenReturn(EntitlementPort.Entitlement.PREMIUM_ACTIVE);

        ObjectNode payload = mapper.createObjectNode();
        payload.put("position", 1);
        payload.putArray("ranking");

        var result = filter.filterPayload(1L, "session.participant_update", payload);

        assertThat(result.has("ranking")).isTrue();
    }

    @Test
    void filterPayload_freeUser_otherEventType_passThrough() {
        when(entitlementPort.getEffectiveEntitlement(1L)).thenReturn(EntitlementPort.Entitlement.FREE);

        ObjectNode payload = mapper.createObjectNode().put("sessionId", 1L);

        var result = filter.filterPayload(1L, "session.started", payload);

        assertThat(result).isEqualTo(payload);
    }
}
