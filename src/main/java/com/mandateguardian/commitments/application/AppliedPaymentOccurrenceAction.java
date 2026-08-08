package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import java.time.LocalDate;
import java.util.UUID;

public record AppliedPaymentOccurrenceAction(
        UUID occurrenceId,
        UUID commitmentId,
        LocalDate dueDate,
        LocalDate originalDueDate,
        PaymentOccurrenceStatus status,
        boolean changed) {
}
