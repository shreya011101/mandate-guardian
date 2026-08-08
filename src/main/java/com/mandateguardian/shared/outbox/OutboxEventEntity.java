package com.mandateguardian.shared.outbox;

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
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "last_error")
    private String lastError;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            JsonNode payload,
            Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.availableAt = occurredAt;
        this.attemptCount = 0;
    }

    public UUID getId() {
        return id;
    }
}
