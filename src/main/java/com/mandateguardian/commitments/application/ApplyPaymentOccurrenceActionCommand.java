package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.PaymentOccurrenceAction;
import java.time.LocalDate;
import java.util.UUID;

public record ApplyPaymentOccurrenceActionCommand(
        UUID occurrenceId,
        PaymentOccurrenceAction action,
        LocalDate postponedDueDate) {
}
