package com.mandateguardian.identity.api;

import com.mandateguardian.identity.application.UserProfileService;
import com.mandateguardian.shared.security.CurrentUser;
import jakarta.validation.Valid;
import java.time.ZoneId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/profile")
class UserProfileController {

    private final CurrentUser currentUser;
    private final UserProfileService userProfileService;

    UserProfileController(CurrentUser currentUser, UserProfileService userProfileService) {
        this.currentUser = currentUser;
        this.userProfileService = userProfileService;
    }

    @PutMapping
    ResponseEntity<UserProfileResponse> upsertProfile(@Valid @RequestBody UpsertUserProfileRequest request) {
        var profile = userProfileService.upsert(currentUser.subject(), ZoneId.of(request.timeZone()));
        return ResponseEntity.ok(UserProfileResponse.from(profile));
    }
}
