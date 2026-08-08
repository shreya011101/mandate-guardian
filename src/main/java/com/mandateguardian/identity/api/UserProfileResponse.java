package com.mandateguardian.identity.api;

import com.mandateguardian.identity.application.UserProfile;
import java.util.UUID;

public record UserProfileResponse(UUID id, String timeZone) {

    static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(profile.id(), profile.timeZone().getId());
    }
}
