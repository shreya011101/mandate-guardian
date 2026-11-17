package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.PaymentMode;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingCommitment(
        UUID commitmentId,
        UUID occurrenceId,
        String displayName,
        CommitmentCategory category,
        LocalDate dueDate,
        long expectedAmountMinor,
        String currency,
        PaymentMode paymentMode,
        PaymentOccurrenceStatus occurrenceStatus) {

    public boolean requiresManualAction() {
        return paymentMode == PaymentMode.MANUAL;
    }
}
