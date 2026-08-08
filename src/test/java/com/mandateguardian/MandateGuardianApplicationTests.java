package com.mandateguardian;

import static org.assertj.core.api.Assertions.assertThat;

import com.mandateguardian.commitments.application.CreateMonthlyCommitmentCommand;
import com.mandateguardian.commitments.application.CreateMonthlyCommitmentService;
import com.mandateguardian.commitments.application.ListMonthlyCommitmentsService;
import com.mandateguardian.commitments.application.MonthlyCommitment;
import com.mandateguardian.commitments.application.ApplyPaymentOccurrenceActionCommand;
import com.mandateguardian.commitments.application.ApplyPaymentOccurrenceActionService;
import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.PaymentOccurrenceAction;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import com.mandateguardian.commitments.domain.PaymentMode;
import com.mandateguardian.commitments.infrastructure.persistence.CommitmentRepository;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceRepository;
import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.audit.AuditEventRepository;
import com.mandateguardian.shared.idempotency.IdempotencyKeyRepository;
import com.mandateguardian.shared.outbox.OutboxEventRepository;
import java.time.YearMonth;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MandateGuardianApplicationTests {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private CreateMonthlyCommitmentService createMonthlyCommitmentService;

    @Autowired
    private ListMonthlyCommitmentsService listMonthlyCommitmentsService;

    @Autowired
    private ApplyPaymentOccurrenceActionService applyPaymentOccurrenceActionService;

    @Autowired
    private CommitmentRepository commitmentRepository;

    @Autowired
    private PaymentOccurrenceRepository paymentOccurrenceRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    @WithMockUser(username = "integration-user")
    void createsCommitmentOccurrenceAuditAndOutboxEventInOneWorkflow() {
        userProfileService.upsert("integration-user", ZoneId.of("Asia/Kolkata"));
        var command = new CreateMonthlyCommitmentCommand(
                "Netflix",
                CommitmentCategory.SUBSCRIPTION,
                64900,
                "INR",
                15,
                PaymentMode.AUTO_DEBIT,
                "netflix-2026-08-08");

        var created = createMonthlyCommitmentService.create(command);
        var replayed = createMonthlyCommitmentService.create(command);

        assertThat(created.reusedIdempotentResult()).isFalse();
        assertThat(replayed).isEqualTo(new com.mandateguardian.commitments.application.CreatedCommitment(
                created.commitmentId(),
                created.occurrenceId(),
                created.dueDate(),
                created.expectedAmountMinor(),
                created.currency(),
                true));
        var paymentAction = applyPaymentOccurrenceActionService.apply(new ApplyPaymentOccurrenceActionCommand(
                created.occurrenceId(),
                PaymentOccurrenceAction.MARK_PAID,
                null));
        var replayedPaymentAction = applyPaymentOccurrenceActionService.apply(new ApplyPaymentOccurrenceActionCommand(
                created.occurrenceId(),
                PaymentOccurrenceAction.MARK_PAID,
                null));

        assertThat(paymentAction.status()).isEqualTo(PaymentOccurrenceStatus.PAID);
        assertThat(paymentAction.changed()).isTrue();
        assertThat(replayedPaymentAction.changed()).isFalse();
        assertThat(commitmentRepository.count()).isEqualTo(1);
        assertThat(paymentOccurrenceRepository.count()).isEqualTo(1);
        assertThat(outboxEventRepository.count()).isEqualTo(2);
        assertThat(auditEventRepository.count()).isEqualTo(2);
        assertThat(idempotencyKeyRepository.count()).isEqualTo(1);
        assertThat(listMonthlyCommitmentsService.list(YearMonth.from(created.dueDate())))
                .containsExactly(new MonthlyCommitment(
                        created.commitmentId(),
                        created.occurrenceId(),
                        "Netflix",
                        CommitmentCategory.SUBSCRIPTION,
                        created.dueDate(),
                        64900,
                        "INR",
                        PaymentMode.AUTO_DEBIT,
                        PaymentOccurrenceStatus.PAID));
    }
}
