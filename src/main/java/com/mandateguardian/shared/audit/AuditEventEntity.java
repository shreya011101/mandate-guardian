package com.mandateguardian.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "audit_events")
public class AuditEventEntity {

    @Id
    private UUID id;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode details;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEventEntity() {
    }

    public AuditEventEntity(
            UUID actorUserId,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            JsonNode details,
            Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.details = details;
        this.occurredAt = occurredAt;
    }
}
