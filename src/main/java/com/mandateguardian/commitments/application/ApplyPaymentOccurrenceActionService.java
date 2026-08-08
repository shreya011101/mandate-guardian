package com.mandateguardian.commitments.application;

import com.mandateguardian.commitments.domain.PaymentOccurrenceAction;
import com.mandateguardian.commitments.domain.PaymentOccurrenceStatus;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceEntity;
import com.mandateguardian.commitments.infrastructure.persistence.PaymentOccurrenceRepository;
import com.mandateguardian.identity.application.UserProfile;
import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.audit.AuditEventEntity;
import com.mandateguardian.shared.audit.AuditEventRepository;
import com.mandateguardian.shared.outbox.OutboxEventEntity;
import com.mandateguardian.shared.outbox.OutboxEventRepository;
import com.mandateguardian.shared.security.CurrentUser;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class ApplyPaymentOccurrenceActionService {

    private static final String PAYMENT_OCCURRENCE_AGGREGATE = "PAYMENT_OCCURRENCE";
    private static final String ACTION_APPLIED_EVENT = "payment_occurrence.action_applied";

    private final CurrentUser currentUser;
    private final UserProfileService userProfileService;
    private final PaymentOccurrenceRepository paymentOccurrenceRepository;
    private final AuditEventRepository auditEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ApplyPaymentOccurrenceActionService(
            CurrentUser currentUser,
            UserProfileService userProfileService,
            PaymentOccurrenceRepository paymentOccurrenceRepository,
            AuditEventRepository auditEventRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            Clock clock) {
        this.currentUser = currentUser;
        this.userProfileService = userProfileService;
        this.paymentOccurrenceRepository = paymentOccurrenceRepository;
        this.auditEventRepository = auditEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public AppliedPaymentOccurrenceAction apply(ApplyPaymentOccurrenceActionCommand command) {
        validate(command);
        UserProfile user = userProfileService.requireActiveProfile(currentUser.subject());
        PaymentOccurrenceEntity occurrence = paymentOccurrenceRepository
                .findByIdAndUserId(command.occurrenceId(), user.id())
                .orElseThrow(() -> new PaymentOccurrenceNotFoundException(command.occurrenceId()));

        PaymentOccurrenceStatus previousStatus = occurrence.getStatus();
        LocalDate previousDueDate = occurrence.getDueDate();
        Instant now = clock.instant();
        boolean changed = occurrence.applyAction(command.action(), command.postponedDueDate(), now);

        if (changed) {
            ObjectNode payload = eventPayload(occurrence, command.action(), previousStatus, previousDueDate);
            auditEventRepository.save(new AuditEventEntity(
                    user.id(),
                    PAYMENT_OCCURRENCE_AGGREGATE,
                    occurrence.getId(),
                    ACTION_APPLIED_EVENT,
                    payload,
                    now));
            outboxEventRepository.save(new OutboxEventEntity(
                    PAYMENT_OCCURRENCE_AGGREGATE,
                    occurrence.getId(),
                    ACTION_APPLIED_EVENT,
                    payload,
                    now));
        }

        return new AppliedPaymentOccurrenceAction(
                occurrence.getId(),
                occurrence.getCommitmentId(),
                occurrence.getDueDate(),
                occurrence.getOriginalDueDate(),
                occurrence.getStatus(),
                changed);
    }

    private void validate(ApplyPaymentOccurrenceActionCommand command) {
        Objects.requireNonNull(command, "A payment occurrence action is required");
        if (command.occurrenceId() == null) {
            throw new IllegalArgumentException("A payment occurrence id is required");
        }
        if (command.action() == null) {
            throw new IllegalArgumentException("A payment occurrence action type is required");
        }
        if (command.action() != PaymentOccurrenceAction.POSTPONE && command.postponedDueDate() != null) {
            throw new IllegalArgumentException("A postponed due date is allowed only when postponing a payment occurrence");
        }
    }

    private ObjectNode eventPayload(
            PaymentOccurrenceEntity occurrence,
            PaymentOccurrenceAction action,
            PaymentOccurrenceStatus previousStatus,
            LocalDate previousDueDate) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("occurrenceId", occurrence.getId().toString());
        payload.put("commitmentId", occurrence.getCommitmentId().toString());
        payload.put("action", action.name());
        payload.put("previousStatus", previousStatus.name());
        payload.put("status", occurrence.getStatus().name());
        payload.put("previousDueDate", previousDueDate.toString());
        payload.put("dueDate", occurrence.getDueDate().toString());
        payload.put("originalDueDate", occurrence.getOriginalDueDate().toString());
        return payload;
    }
}
