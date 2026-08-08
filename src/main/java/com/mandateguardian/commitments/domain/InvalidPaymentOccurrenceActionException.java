package com.mandateguardian.commitments.domain;

public class InvalidPaymentOccurrenceActionException extends RuntimeException {

    public InvalidPaymentOccurrenceActionException(String message) {
        super(message);
    }
}
