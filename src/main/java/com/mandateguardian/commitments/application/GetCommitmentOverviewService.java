package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.PaymentMode;
import com.mandateguardian.commitments.domain.CommitmentStatus;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceRepository;
import com.mandateguardian.commitments.infrastructure.persistence.UpcomingCommitmentRow;
import com.mandateguardian.identity.application.UserProfile;
import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.security.CurrentUser;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCommitmentOverviewService {

    private static final int MAXIMUM_FORECAST_DAYS = 90;
    private static final EnumSet<PaymentOccurrenceStatus> OPEN_STATUSES = EnumSet.of(
            PaymentOccurrenceStatus.PLANNED,
            PaymentOccurrenceStatus.DUE,
            PaymentOccurrenceStatus.POSTPONED,
            PaymentOccurrenceStatus.OVERDUE);

    private final PaymentOccurrenceRepository paymentOccurrenceRepository;
    private final UserProfileService userProfileService;
    private final CurrentUser currentUser;
    private final Clock clock;

    public GetCommitmentOverviewService(
            PaymentOccurrenceRepository paymentOccurrenceRepository,
            UserProfileService userProfileService,
            CurrentUser currentUser,
            Clock clock) {
        this.paymentOccurrenceRepository = paymentOccurrenceRepository;
        this.userProfileService = userProfileService;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public CommitmentOverview get(int days) {
        if (days < 1 || days > MAXIMUM_FORECAST_DAYS) {
            throw new IllegalArgumentException("Forecast days must be between 1 and " + MAXIMUM_FORECAST_DAYS);
        }

        UserProfile user = userProfileService.requireActiveProfile(currentUser.subject());
        LocalDate periodStart = LocalDate.now(clock.withZone(user.timeZone()));
        LocalDate periodEndExclusive = periodStart.plusDays(days);
        List<UpcomingCommitment> payments = paymentOccurrenceRepository
                .findUpcomingCommitments(
                        user.id(),
                        periodStart,
                        periodEndExclusive,
                        CommitmentStatus.ACTIVE,
                        OPEN_STATUSES)
                .stream()
                .map(this::toUpcomingCommitment)
                .toList();

        long totalExpectedAmountMinor = payments.stream()
                .mapToLong(UpcomingCommitment::expectedAmountMinor)
                .sum();
        long manualActionAmountMinor = payments.stream()
                .filter(UpcomingCommitment::requiresManualAction)
                .mapToLong(UpcomingCommitment::expectedAmountMinor)
                .sum();
        long autoDebitAmountMinor = payments.stream()
                .filter(payment -> payment.paymentMode() == PaymentMode.AUTO_DEBIT)
                .mapToLong(UpcomingCommitment::expectedAmountMinor)
                .sum();
        int manualActionCount = (int) payments.stream()
                .filter(UpcomingCommitment::requiresManualAction)
                .count();

        return new CommitmentOverview(
                periodStart,
                periodEndExclusive,
                payments.isEmpty() ? "INR" : payments.getFirst().currency(),
                totalExpectedAmountMinor,
                manualActionAmountMinor,
                autoDebitAmountMinor,
                manualActionCount,
                payments);
    }

    private UpcomingCommitment toUpcomingCommitment(UpcomingCommitmentRow row) {
        return new UpcomingCommitment(
                row.commitmentId(),
                row.occurrenceId(),
                row.displayName(),
                row.category(),
                row.dueDate(),
                row.expectedAmountMinor(),
                row.currency(),
                row.paymentMode(),
                row.occurrenceStatus());
    }
}
