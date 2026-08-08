package com.mandateguardian.identity.application;

import java.time.ZoneId;
import java.util.UUID;

public record UserProfile(UUID id, String subject, ZoneId timeZone) {
}
