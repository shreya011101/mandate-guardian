package com.mandateguardian.identity.api;

import jakarta.validation.constraints.NotBlank;

public record UpsertUserProfileRequest(@NotBlank String timeZone) {
}
