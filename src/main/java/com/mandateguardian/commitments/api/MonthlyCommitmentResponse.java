package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.MonthlyCommitment;
import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.PaymentMode;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import java.time.LocalDate;
import java.util.UUID;

record MonthlyCommitmentResponse(
        UUID commitmentId,
        UUID occurrenceId,
        String displayName,
        CommitmentCategory category,
        LocalDate dueDate,
        long expectedAmountMinor,
        String currency,
        PaymentMode paymentMode,
        PaymentOccurrenceStatus occurrenceStatus) {

    static MonthlyCommitmentResponse from(MonthlyCommitment commitment) {
        return new MonthlyCommitmentResponse(
                commitment.commitmentId(),
                commitment.occurrenceId(),
                commitment.displayName(),
                commitment.category(),
                commitment.dueDate(),
                commitment.expectedAmountMinor(),
                commitment.currency(),
                commitment.paymentMode(),
                commitment.occurrenceStatus());
    }
}
