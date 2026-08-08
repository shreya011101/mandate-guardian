package com.mandateguardian.commitments.application;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String idempotencyKey) {
        super("The idempotency key was already used for a different request: " + idempotencyKey);
    }
}
