package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.domain.PaymentOccurrenceAction;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

record ApplyPaymentOccurrenceActionRequest(
        @NotNull PaymentOccurrenceAction action,
        LocalDate postponedDueDate) {
}
