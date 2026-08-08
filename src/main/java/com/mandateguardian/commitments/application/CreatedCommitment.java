package com.mandateguardian.commitments.application;

import java.time.LocalDate;
import java.util.UUID;

public record CreatedCommitment(
        UUID commitmentId,
        UUID occurrenceId,
        LocalDate dueDate,
        long expectedAmountMinor,
        String currency,
        boolean reusedIdempotentResult) {
}
