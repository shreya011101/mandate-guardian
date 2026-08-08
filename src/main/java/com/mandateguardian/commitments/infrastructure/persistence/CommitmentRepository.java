package com.mandateguardian.commitments.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitmentRepository extends JpaRepository<CommitmentEntity, UUID> {

    Optional<CommitmentEntity> findByIdAndUserId(UUID id, UUID userId);
}
