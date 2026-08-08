package com.mandateguardian.commitments.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mandateguardian.commitments.domain.InvalidPaymentOccurrenceActionException;
import com.mandateguardian.commitments.domain.PaymentOccurrenceAction;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentOccurrenceEntityTest {

    private static final Instant CREATED_AT = Instant.parse("2026-08-08T00:00:00Z");

    @Test
    void marksAnActionableOccurrencePaidAndTreatsAnExactRetryAsANoOp() {
        PaymentOccurrenceEntity occurrence = occurrence();

        assertThat(occurrence.applyAction(PaymentOccurrenceAction.MARK_PAID, null, CREATED_AT.plusSeconds(1)))
                .isTrue();
        assertThat(occurrence.getStatus()).isEqualTo(PaymentOccurrenceStatus.PAID);
        assertThat(occurrence.applyAction(PaymentOccurrenceAction.MARK_PAID, null, CREATED_AT.plusSeconds(2)))
                .isFalse();
        assertThatThrownBy(() -> occurrence.applyAction(PaymentOccurrenceAction.SKIP, null, CREATED_AT.plusSeconds(3)))
                .isInstanceOf(InvalidPaymentOccurrenceActionException.class);
    }

    @Test
    void postponesToALaterDateAndTreatsAnExactRetryAsANoOp() {
        PaymentOccurrenceEntity occurrence = occurrence();
        LocalDate postponedDueDate = LocalDate.of(2026, 8, 20);

        assertThat(occurrence.applyAction(PaymentOccurrenceAction.POSTPONE, postponedDueDate, CREATED_AT.plusSeconds(1)))
                .isTrue();
        assertThat(occurrence.getStatus()).isEqualTo(PaymentOccurrenceStatus.POSTPONED);
        assertThat(occurrence.getDueDate()).isEqualTo(postponedDueDate);
        assertThat(occurrence.applyAction(PaymentOccurrenceAction.POSTPONE, postponedDueDate, CREATED_AT.plusSeconds(2)))
                .isFalse();
    }

    @Test
    void rejectsAPostponementThatDoesNotMoveTheDueDateForward() {
        PaymentOccurrenceEntity occurrence = occurrence();

        assertThatThrownBy(() -> occurrence.applyAction(
                        PaymentOccurrenceAction.POSTPONE,
                        LocalDate.of(2026, 8, 15),
                        CREATED_AT.plusSeconds(1)))
                .isInstanceOf(InvalidPaymentOccurrenceActionException.class)
                .hasMessage("A postponed due date must be after the current due date");
    }

    private PaymentOccurrenceEntity occurrence() {
        return new PaymentOccurrenceEntity(
                UUID.randomUUID(),
                LocalDate.of(2026, 8, 15),
                64900,
                "INR",
                CREATED_AT);
    }
}
