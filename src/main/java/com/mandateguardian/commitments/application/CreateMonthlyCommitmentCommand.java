package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.PaymentMode;

public record CreateMonthlyCommitmentCommand(
        String displayName,
        CommitmentCategory category,
        long expectedAmountMinor,
        String currency,
        int dueDay,
        PaymentMode paymentMode,
        String idempotencyKey) {
}
