package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.AppliedPaymentOccurrenceAction;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import java.time.LocalDate;
import java.util.UUID;

record AppliedPaymentOccurrenceActionResponse(
        UUID occurrenceId,
        UUID commitmentId,
        LocalDate dueDate,
        LocalDate originalDueDate,
        PaymentOccurrenceStatus status,
        boolean changed) {

    static AppliedPaymentOccurrenceActionResponse from(AppliedPaymentOccurrenceAction appliedAction) {
        return new AppliedPaymentOccurrenceActionResponse(
                appliedAction.occurrenceId(),
                appliedAction.commitmentId(),
                appliedAction.dueDate(),
                appliedAction.originalDueDate(),
                appliedAction.status(),
                appliedAction.changed());
    }
}
