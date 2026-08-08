package com.mandateguardian.commitments.infrastructure.persistence;

import com.mandateguardian.commitments.domain.InvalidPaymentOccurrenceActionException;
import com.mandateguardian.commitments.domain.PaymentOccurrenceAction;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "payment_occurrences")
public class PaymentOccurrenceEntity {

    @Id
    private UUID id;

    @Column(name = "commitment_id", nullable = false)
    private UUID commitmentId;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "original_due_date", nullable = false)
    private LocalDate originalDueDate;

    @Column(name = "expected_amount_minor", nullable = false)
    private long expectedAmountMinor;

    @JdbcTypeCode(Types.CHAR)
    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentOccurrenceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected PaymentOccurrenceEntity() {
    }

    public PaymentOccurrenceEntity(
            UUID commitmentId,
            LocalDate dueDate,
            long expectedAmountMinor,
            String currency,
            Instant now) {
        this.id = UUID.randomUUID();
        this.commitmentId = commitmentId;
        this.dueDate = dueDate;
        this.originalDueDate = dueDate;
        this.expectedAmountMinor = expectedAmountMinor;
        this.currency = currency;
        this.status = PaymentOccurrenceStatus.PLANNED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCommitmentId() {
        return commitmentId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public long getExpectedAmountMinor() {
        return expectedAmountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getOriginalDueDate() {
        return originalDueDate;
    }

    public PaymentOccurrenceStatus getStatus() {
        return status;
    }

    public boolean applyAction(PaymentOccurrenceAction action, LocalDate postponedDueDate, Instant now) {
        return switch (action) {
            case MARK_PAID -> transitionTo(PaymentOccurrenceStatus.PAID, action, now);
            case SKIP -> transitionTo(PaymentOccurrenceStatus.SKIPPED, action, now);
            case POSTPONE -> postpone(postponedDueDate, now);
        };
    }

    private boolean transitionTo(
            PaymentOccurrenceStatus target,
            PaymentOccurrenceAction action,
            Instant now) {
        if (status == target) {
            return false;
        }
        requireActionable(action);
        status = target;
        updatedAt = now;
        return true;
    }

    private boolean postpone(LocalDate postponedDueDate, Instant now) {
        if (postponedDueDate == null) {
            throw new InvalidPaymentOccurrenceActionException(
                    "A postponed due date is required when postponing a payment occurrence");
        }
        if (status == PaymentOccurrenceStatus.POSTPONED && dueDate.equals(postponedDueDate)) {
            return false;
        }
        if (!postponedDueDate.isAfter(dueDate)) {
            throw new InvalidPaymentOccurrenceActionException(
                    "A postponed due date must be after the current due date");
        }
        requireActionable(PaymentOccurrenceAction.POSTPONE);
        dueDate = postponedDueDate;
        status = PaymentOccurrenceStatus.POSTPONED;
        updatedAt = now;
        return true;
    }

    private void requireActionable(PaymentOccurrenceAction action) {
        if (status == PaymentOccurrenceStatus.PAID || status == PaymentOccurrenceStatus.SKIPPED) {
            throw new InvalidPaymentOccurrenceActionException(
                    "Cannot apply " + action + " to a " + status + " payment occurrence");
        }
    }
}
