package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.CreatedCommitment;
import java.time.LocalDate;
import java.util.UUID;

public record CreatedCommitmentResponse(
        UUID commitmentId,
        UUID occurrenceId,
        LocalDate dueDate,
        long expectedAmountMinor,
        String currency,
        boolean reusedIdempotentResult) {

    static CreatedCommitmentResponse from(CreatedCommitment result) {
        return new CreatedCommitmentResponse(
                result.commitmentId(),
                result.occurrenceId(),
                result.dueDate(),
                result.expectedAmountMinor(),
                result.currency(),
                result.reusedIdempotentResult());
    }
}
