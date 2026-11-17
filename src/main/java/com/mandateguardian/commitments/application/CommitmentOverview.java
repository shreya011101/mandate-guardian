package com.mandateguardian.commitments.application;

import java.time.LocalDate;
import java.util.List;

public record CommitmentOverview(
        LocalDate periodStart,
        LocalDate periodEndExclusive,
        String currency,
        long totalExpectedAmountMinor,
        long manualActionAmountMinor,
        long autoDebitAmountMinor,
        int manualActionCount,
        List<UpcomingCommitment> upcomingPayments) {

    public int upcomingPaymentCount() {
        return upcomingPayments.size();
    }
}
