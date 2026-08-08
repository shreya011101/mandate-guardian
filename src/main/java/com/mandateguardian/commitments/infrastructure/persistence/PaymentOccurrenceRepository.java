package com.mandateguardian.commitments.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOccurrenceRepository extends JpaRepository<PaymentOccurrenceEntity, UUID> {

    Optional<PaymentOccurrenceEntity> findByCommitmentIdAndDueDate(UUID commitmentId, LocalDate dueDate);

    @Query("""
            select occurrence
            from PaymentOccurrenceEntity occurrence
            join CommitmentEntity commitment on commitment.id = occurrence.commitmentId
            where occurrence.id = :occurrenceId
              and commitment.userId = :userId
            """)
    Optional<PaymentOccurrenceEntity> findByIdAndUserId(
            @Param("occurrenceId") UUID occurrenceId,
            @Param("userId") UUID userId);

    @Query("""
            select new com.mandateguardian.commitments.infrastructure.persistence.MonthlyCommitmentRow(
                commitment.id,
                occurrence.id,
                commitment.displayName,
                commitment.category,
                occurrence.dueDate,
                occurrence.expectedAmountMinor,
                occurrence.currency,
                commitment.paymentMode,
                occurrence.status)
            from PaymentOccurrenceEntity occurrence
            join CommitmentEntity commitment on commitment.id = occurrence.commitmentId
            where commitment.userId = :userId
              and occurrence.dueDate >= :startInclusive
              and occurrence.dueDate < :endExclusive
            order by occurrence.dueDate asc, commitment.displayName asc
            """)
    List<MonthlyCommitmentRow> findMonthlyCommitments(
            @Param("userId") UUID userId,
            @Param("startInclusive") LocalDate startInclusive,
            @Param("endExclusive") LocalDate endExclusive);
}
