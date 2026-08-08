package com.mandateguardian.commitments.infrastructure.persistence;

import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.CommitmentStatus;
import com.mandateguardian.commitments.domain.PaymentMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "commitments")
public class CommitmentEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CommitmentCategory category;

    @Column(name = "expected_amount_minor", nullable = false)
    private long expectedAmountMinor;

    @JdbcTypeCode(Types.CHAR)
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "recurrence_type", nullable = false, length = 16)
    private String recurrenceType;

    @JdbcTypeCode(Types.SMALLINT)
    @Column(name = "due_day", nullable = false)
    private int dueDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 16)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CommitmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected CommitmentEntity() {
    }

    public CommitmentEntity(
            UUID userId,
            String displayName,
            CommitmentCategory category,
            long expectedAmountMinor,
            String currency,
            int dueDay,
            PaymentMode paymentMode,
            Instant now) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.displayName = displayName;
        this.category = category;
        this.expectedAmountMinor = expectedAmountMinor;
        this.currency = currency;
        this.recurrenceType = "MONTHLY";
        this.dueDay = dueDay;
        this.paymentMode = paymentMode;
        this.status = CommitmentStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CommitmentCategory getCategory() {
        return category;
    }

    public long getExpectedAmountMinor() {
        return expectedAmountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public int getDueDay() {
        return dueDay;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }
}
