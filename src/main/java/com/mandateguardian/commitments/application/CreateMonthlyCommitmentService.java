package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.MonthlyDueDateCalculator;
import com.mandateguardian.commitments.infrastructure.persistence.CommitmentEntity;
import com.mandateguardian.commitments.infrastructure.persistence.CommitmentRepository;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceEntity;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceRepository;
import com.mandateguardian.identity.application.UserProfile;
import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.audit.AuditEventEntity;
import com.mandateguardian.shared.audit.AuditEventRepository;
import com.mandateguardian.shared.idempotency.IdempotencyKeyEntity;
import com.mandateguardian.shared.idempotency.IdempotencyKeyId;
import com.mandateguardian.shared.idempotency.IdempotencyKeyRepository;
import com.mandateguardian.shared.outbox.OutboxEventEntity;
import com.mandateguardian.shared.outbox.OutboxEventRepository;
import com.mandateguardian.shared.security.CurrentUser;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class CreateMonthlyCommitmentService {

    private static final String COMMITMENT_AGGREGATE = "COMMITMENT";
    private static final String COMMITMENT_CREATED_EVENT = "commitment.created";

    private final CurrentUser currentUser;
    private final UserProfileService userProfileService;
    private final CommitmentRepository commitmentRepository;
    private final PaymentOccurrenceRepository paymentOccurrenceRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final AuditEventRepository auditEventRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final MonthlyDueDateCalculator dueDateCalculator = new MonthlyDueDateCalculator();

    public CreateMonthlyCommitmentService(
            CurrentUser currentUser,
            UserProfileService userProfileService,
            CommitmentRepository commitmentRepository,
            PaymentOccurrenceRepository paymentOccurrenceRepository,
            OutboxEventRepository outboxEventRepository,
            AuditEventRepository auditEventRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            ObjectMapper objectMapper,
            Clock clock) {
        this.currentUser = currentUser;
        this.userProfileService = userProfileService;
        this.commitmentRepository = commitmentRepository;
        this.paymentOccurrenceRepository = paymentOccurrenceRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.auditEventRepository = auditEventRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public CreatedCommitment create(CreateMonthlyCommitmentCommand command) {
        NormalizedCommand normalized = normalizeAndValidate(command);
        UserProfile user = userProfileService.requireActiveProfile(currentUser.subject());
        String fingerprint = fingerprint(user, normalized);
        IdempotencyKeyId idempotencyKeyId = new IdempotencyKeyId(user.id(), normalized.idempotencyKey());

        var existing = idempotencyKeyRepository.findById(idempotencyKeyId);
        if (existing.isPresent()) {
            return reuseExistingResult(existing.get(), user, fingerprint, normalized.idempotencyKey());
        }

        Instant now = clock.instant();
        LocalDate dueDate = dueDateCalculator.nextDueDate(
                LocalDate.now(clock.withZone(user.timeZone())), normalized.dueDay());

        CommitmentEntity commitment = new CommitmentEntity(
                user.id(),
                normalized.displayName(),
                normalized.category(),
                normalized.expectedAmountMinor(),
                normalized.currency(),
                normalized.dueDay(),
                normalized.paymentMode(),
                now);
        commitmentRepository.save(commitment);

        PaymentOccurrenceEntity occurrence = new PaymentOccurrenceEntity(
                commitment.getId(),
                dueDate,
                normalized.expectedAmountMinor(),
                normalized.currency(),
                now);
        paymentOccurrenceRepository.save(occurrence);

        ObjectNode eventPayload = eventPayload(commitment, occurrence, user.timeZone());
        outboxEventRepository.save(new OutboxEventEntity(
                COMMITMENT_AGGREGATE,
                commitment.getId(),
                COMMITMENT_CREATED_EVENT,
                eventPayload,
                now));
        auditEventRepository.save(new AuditEventEntity(
                user.id(),
                COMMITMENT_AGGREGATE,
                commitment.getId(),
                COMMITMENT_CREATED_EVENT,
                eventPayload,
                now));
        idempotencyKeyRepository.save(new IdempotencyKeyEntity(
                user.id(),
                normalized.idempotencyKey(),
                fingerprint,
                commitment.getId(),
                occurrence.getId(),
                now,
                now.plus(24, ChronoUnit.HOURS)));

        return result(commitment, occurrence, false);
    }

    private CreatedCommitment reuseExistingResult(
            IdempotencyKeyEntity existing,
            UserProfile user,
            String fingerprint,
            String idempotencyKey) {
        if (!existing.getRequestFingerprint().equals(fingerprint)) {
            throw new IdempotencyConflictException(idempotencyKey);
        }

        CommitmentEntity commitment = commitmentRepository.findByIdAndUserId(existing.getResourceId(), user.id())
                .orElseThrow(() -> new IllegalStateException("Idempotency record references a missing commitment"));
        PaymentOccurrenceEntity occurrence = paymentOccurrenceRepository.findById(existing.getOccurrenceId())
                .orElseThrow(() -> new IllegalStateException("Idempotency record references a missing occurrence"));
        return result(commitment, occurrence, true);
    }

    private ObjectNode eventPayload(CommitmentEntity commitment, PaymentOccurrenceEntity occurrence, ZoneId timeZone) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("commitmentId", commitment.getId().toString());
        payload.put("occurrenceId", occurrence.getId().toString());
        payload.put("dueDate", occurrence.getDueDate().toString());
        payload.put("amountMinor", occurrence.getExpectedAmountMinor());
        payload.put("currency", occurrence.getCurrency());
        payload.put("timeZone", timeZone.getId());
        payload.put("paymentMode", commitment.getPaymentMode().name());
        return payload;
    }

    private CreatedCommitment result(
            CommitmentEntity commitment,
            PaymentOccurrenceEntity occurrence,
            boolean reusedIdempotentResult) {
        return new CreatedCommitment(
                commitment.getId(),
                occurrence.getId(),
                occurrence.getDueDate(),
                occurrence.getExpectedAmountMinor(),
                occurrence.getCurrency(),
                reusedIdempotentResult);
    }

    private NormalizedCommand normalizeAndValidate(CreateMonthlyCommitmentCommand command) {
        Objects.requireNonNull(command, "A commitment command is required");
        String displayName = requireNonBlank(command.displayName(), "A display name is required");
        String currency = requireNonBlank(command.currency(), "A currency is required").toUpperCase(Locale.ROOT);
        String idempotencyKey = requireNonBlank(command.idempotencyKey(), "An idempotency key is required");

        if (command.category() == null) {
            throw new IllegalArgumentException("A commitment category is required");
        }
        if (command.paymentMode() == null) {
            throw new IllegalArgumentException("A payment mode is required");
        }
        if (command.expectedAmountMinor() < 0) {
            throw new IllegalArgumentException("The expected amount cannot be negative");
        }
        if (!"INR".equals(currency)) {
            throw new IllegalArgumentException("The MVP accepts INR commitments only");
        }
        if (command.dueDay() < 1 || command.dueDay() > 31) {
            throw new IllegalArgumentException("A monthly due day must be between 1 and 31");
        }

        return new NormalizedCommand(
                displayName,
                command.category(),
                command.expectedAmountMinor(),
                currency,
                command.dueDay(),
                command.paymentMode(),
                idempotencyKey);
    }

    private String fingerprint(UserProfile user, NormalizedCommand command) {
        String material = String.join(
                "|",
                user.id().toString(),
                command.displayName(),
                command.category().name(),
                Long.toString(command.expectedAmountMinor()),
                command.currency(),
                Integer.toString(command.dueDay()),
                command.paymentMode().name());
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private record NormalizedCommand(
            String displayName,
            com.mandateguardian.commitments.domain.CommitmentCategory category,
            long expectedAmountMinor,
            String currency,
            int dueDay,
            com.mandateguardian.commitments.domain.PaymentMode paymentMode,
            String idempotencyKey) {
    }
}
