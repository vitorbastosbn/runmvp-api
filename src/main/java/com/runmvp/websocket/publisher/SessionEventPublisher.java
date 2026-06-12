package com.runmvp.websocket.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runmvp.session.application.port.out.SessionParticipantRepository;
import com.runmvp.session.domain.SessionParticipant;
import com.runmvp.websocket.entitlement.EntitlementFilter;
import com.runmvp.websocket.registry.WebSocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class SessionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SessionEventPublisher.class);

    private final JdbcTemplate jdbc;
    private final WebSocketSessionRegistry registry;
    private final SessionParticipantRepository participantRepository;
    private final EntitlementFilter entitlementFilter;
    private final ObjectMapper mapper;

    public SessionEventPublisher(JdbcTemplate jdbc,
                                 WebSocketSessionRegistry registry,
                                 SessionParticipantRepository participantRepository,
                                 EntitlementFilter entitlementFilter,
                                 ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.registry = registry;
        this.participantRepository = participantRepository;
        this.entitlementFilter = entitlementFilter;
        this.mapper = mapper;
    }

    @Scheduled(fixedDelay = 500)
    @Transactional
    public void poll() {
        List<OutboxRow> rows = jdbc.query(
            """
            SELECT id, aggregate_type, aggregate_id, event_type, payload
            FROM outbox_events
            WHERE processed_at IS NULL
            ORDER BY created_at
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """,
            (rs, i) -> new OutboxRow(
                rs.getLong("id"),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getString("payload")
            )
        );

        for (OutboxRow row : rows) {
            try {
                dispatchEvent(row);
                jdbc.update("UPDATE outbox_events SET processed_at = now() WHERE id = ?", row.id);
            } catch (Exception e) {
                log.error("Failed to dispatch outbox event {}: {}", row.id, e.getMessage());
            }
        }
    }

    private void dispatchEvent(OutboxRow row) throws Exception {
        if (!"RunningSession".equals(row.aggregateType)) return;

        Long sessionId = Long.parseLong(row.aggregateId);
        JsonNode payload = mapper.readTree(row.payload);

        List<SessionParticipant> participants = participantRepository.findBySessionId(sessionId);

        for (SessionParticipant participant : participants) {
            registry.get(participant.getUserId()).ifPresent(wsSession -> {
                try {
                    send(wsSession, participant.getUserId(), row.eventType, payload);
                } catch (IOException e) {
                    log.warn("Failed to send WS to user {}: {}", participant.getUserId(), e.getMessage());
                }
            });
        }
    }

    private void send(WebSocketSession wsSession, Long userId, String eventType, JsonNode payload)
            throws IOException {
        JsonNode filtered = entitlementFilter.filterPayload(userId, eventType, payload);
        ObjectNode envelope = mapper.createObjectNode()
            .put("eventId", UUID.randomUUID().toString())
            .put("type", eventType);
        envelope.set("payload", filtered);
        wsSession.sendMessage(new TextMessage(mapper.writeValueAsString(envelope)));
    }

    record OutboxRow(Long id, String aggregateType, String aggregateId,
                     String eventType, String payload) {}
}
