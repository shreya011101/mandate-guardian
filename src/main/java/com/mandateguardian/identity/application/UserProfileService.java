package com.mandateguardian.identity.application;

import com.mandateguardian.identity.domain.UserStatus;
import com.mandateguardian.identity.infrastructure.persistence.UserEntity;
import com.mandateguardian.identity.infrastructure.persistence.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final Clock clock;

    public UserProfileService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public UserProfile upsert(String subject, ZoneId timeZone) {
        Instant now = clock.instant();
        UserEntity user = userRepository.findBySubject(subject)
                .map(existing -> {
                    existing.updateTimeZone(timeZone, now);
                    return existing;
                })
                .orElseGet(() -> new UserEntity(subject, timeZone, now));

        UserEntity savedUser = userRepository.save(user);
        return toProfile(savedUser);
    }

    @Transactional(readOnly = true)
    public UserProfile requireActiveProfile(String subject) {
        UserEntity user = userRepository.findBySubject(subject)
                .filter(existing -> existing.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new UserNotFoundException(subject));
        return toProfile(user);
    }

    private UserProfile toProfile(UserEntity user) {
        return new UserProfile(user.getId(), user.getSubject(), user.getTimeZone());
    }
}
