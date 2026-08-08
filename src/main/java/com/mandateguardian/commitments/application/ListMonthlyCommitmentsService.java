package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.infrastructure.persistence.MonthlyCommitmentRow;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceRepository;
import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.security.CurrentUser;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMonthlyCommitmentsService {

    private final PaymentOccurrenceRepository paymentOccurrenceRepository;
    private final UserProfileService userProfileService;
    private final CurrentUser currentUser;

    public ListMonthlyCommitmentsService(
            PaymentOccurrenceRepository paymentOccurrenceRepository,
            UserProfileService userProfileService,
            CurrentUser currentUser) {
        this.paymentOccurrenceRepository = paymentOccurrenceRepository;
        this.userProfileService = userProfileService;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public List<MonthlyCommitment> list(YearMonth month) {
        if (month == null) {
            throw new IllegalArgumentException("A month is required");
        }

        var user = userProfileService.requireActiveProfile(currentUser.subject());
        LocalDate startInclusive = month.atDay(1);
        LocalDate endExclusive = month.plusMonths(1).atDay(1);

        return paymentOccurrenceRepository
                .findMonthlyCommitments(user.id(), startInclusive, endExclusive)
                .stream()
                .map(this::toMonthlyCommitment)
                .toList();
    }

    private MonthlyCommitment toMonthlyCommitment(MonthlyCommitmentRow row) {
        return new MonthlyCommitment(
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
