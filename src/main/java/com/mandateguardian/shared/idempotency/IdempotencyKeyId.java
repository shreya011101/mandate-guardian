package com.mandateguardian.shared.idempotency;

import java.io.Serializable;
import java.util.UUID;

public record IdempotencyKeyId(UUID userId, String idempotencyKey) implements Serializable {
}
