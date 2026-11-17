package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.CommitmentOverview;
import com.mandateguardian.commitments.application.UpcomingCommitment;
import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.PaymentMode;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record CommitmentOverviewResponse(
        LocalDate periodStart,
        LocalDate periodEndExclusive,
        String currency,
        long totalExpectedAmountMinor,
        long manualActionAmountMinor,
        long autoDebitAmountMinor,
        int manualActionCount,
        int upcomingPaymentCount,
        List<UpcomingPaymentResponse> upcomingPayments) {

    static CommitmentOverviewResponse from(CommitmentOverview overview) {
        return new CommitmentOverviewResponse(
                overview.periodStart(),
                overview.periodEndExclusive(),
                overview.currency(),
                overview.totalExpectedAmountMinor(),
                overview.manualActionAmountMinor(),
                overview.autoDebitAmountMinor(),
                overview.manualActionCount(),
                overview.upcomingPaymentCount(),
                overview.upcomingPayments().stream().map(UpcomingPaymentResponse::from).toList());
    }

    record UpcomingPaymentResponse(
            UUID commitmentId,
            UUID occurrenceId,
            String displayName,
            CommitmentCategory category,
            LocalDate dueDate,
            long expectedAmountMinor,
            String currency,
            PaymentMode paymentMode,
            PaymentOccurrenceStatus occurrenceStatus,
            boolean requiresManualAction) {

        static UpcomingPaymentResponse from(UpcomingCommitment payment) {
            return new UpcomingPaymentResponse(
                    payment.commitmentId(),
                    payment.occurrenceId(),
                    payment.displayName(),
                    payment.category(),
                    payment.dueDate(),
                    payment.expectedAmountMinor(),
                    payment.currency(),
                    payment.paymentMode(),
                    payment.occurrenceStatus(),
                    payment.requiresManualAction());
        }
    }
}
