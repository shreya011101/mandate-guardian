package com.mandateguardian.commitments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.CommitmentStatus;
import com.mandateguardian.commitments.domain.PaymentMode;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceRepository;
import com.mandateguardian.commitments.infrastructure.persistence.UpcomingCommitmentRow;
import com.mandateguardian.identity.application.UserProfile;
import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.security.CurrentUser;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCommitmentOverviewServiceTest {

    private static final UUID USER_ID = UUID.fromString("d39608fd-1d24-46b1-ac1d-46a9c48b7b45");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-08T20:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private PaymentOccurrenceRepository paymentOccurrenceRepository;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private CurrentUser currentUser;

    @Test
    void returnsATimeZoneAwareCashForecastAndSeparatesManualPayments() {
        when(currentUser.subject()).thenReturn("shreya");
        when(userProfileService.requireActiveProfile("shreya"))
                .thenReturn(new UserProfile(USER_ID, "shreya", ZoneId.of("Asia/Kolkata")));
        when(paymentOccurrenceRepository.findUpcomingCommitments(
                        eq(USER_ID),
                        eq(LocalDate.of(2026, 8, 9)),
                        eq(LocalDate.of(2026, 9, 8)),
                        eq(CommitmentStatus.ACTIVE),
                        any()))
                .thenReturn(List.of(
                        row("Rent", CommitmentCategory.RENT, LocalDate.of(2026, 8, 10), 2500000, PaymentMode.MANUAL),
                        row("Netflix", CommitmentCategory.SUBSCRIPTION, LocalDate.of(2026, 8, 15), 64900, PaymentMode.AUTO_DEBIT)));

        CommitmentOverview overview = service().get(30);

        assertThat(overview.periodStart()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(overview.periodEndExclusive()).isEqualTo(LocalDate.of(2026, 9, 8));
        assertThat(overview.currency()).isEqualTo("INR");
        assertThat(overview.totalExpectedAmountMinor()).isEqualTo(2564900);
        assertThat(overview.manualActionAmountMinor()).isEqualTo(2500000);
        assertThat(overview.autoDebitAmountMinor()).isEqualTo(64900);
        assertThat(overview.manualActionCount()).isEqualTo(1);
        assertThat(overview.upcomingPaymentCount()).isEqualTo(2);
        assertThat(overview.upcomingPayments().getFirst().requiresManualAction()).isTrue();
        assertThat(overview.upcomingPayments().getLast().requiresManualAction()).isFalse();
        verify(paymentOccurrenceRepository).findUpcomingCommitments(
                eq(USER_ID),
                eq(LocalDate.of(2026, 8, 9)),
                eq(LocalDate.of(2026, 9, 8)),
                eq(CommitmentStatus.ACTIVE),
                any());
    }

    @Test
    void rejectsForecastWindowsOutsideTheSupportedRange() {
        assertThatThrownBy(() -> service().get(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Forecast days must be between 1 and 90");
    }

    private GetCommitmentOverviewService service() {
        return new GetCommitmentOverviewService(
                paymentOccurrenceRepository,
                userProfileService,
                currentUser,
                CLOCK);
    }

    private UpcomingCommitmentRow row(
            String displayName,
            CommitmentCategory category,
            LocalDate dueDate,
            long amountMinor,
            PaymentMode paymentMode) {
        return new UpcomingCommitmentRow(
                UUID.randomUUID(),
                UUID.randomUUID(),
                displayName,
                category,
                dueDate,
                amountMinor,
                "INR",
                paymentMode,
                PaymentOccurrenceStatus.PLANNED);
    }
}
