package com.mandateguardian.shared.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@IdClass(IdempotencyKeyId.class)
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Column(name = "response_status", nullable = false)
    private short responseStatus;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "occurrence_id", nullable = false)
    private UUID occurrenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected IdempotencyKeyEntity() {
    }

    public IdempotencyKeyEntity(
            UUID userId,
            String idempotencyKey,
            String requestFingerprint,
            UUID resourceId,
            UUID occurrenceId,
            Instant createdAt,
            Instant expiresAt) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.responseStatus = 201;
        this.resourceId = resourceId;
        this.occurrenceId = occurrenceId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getRequestFingerprint() {
        return requestFingerprint;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getOccurrenceId() {
        return occurrenceId;
    }
}
