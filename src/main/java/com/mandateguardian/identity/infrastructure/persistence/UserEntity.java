package com.mandateguardian.identity.infrastructure.persistence;

import com.mandateguardian.identity.domain.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String subject;

    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected UserEntity() {
    }

    public UserEntity(String subject, ZoneId timeZone, Instant now) {
        this.id = UUID.randomUUID();
        this.subject = subject;
        this.timeZone = timeZone.getId();
        this.status = UserStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public ZoneId getTimeZone() {
        return ZoneId.of(timeZone);
    }

    public UserStatus getStatus() {
        return status;
    }

    public void updateTimeZone(ZoneId newTimeZone, Instant now) {
        this.timeZone = newTimeZone.getId();
        this.updatedAt = now;
    }
}
