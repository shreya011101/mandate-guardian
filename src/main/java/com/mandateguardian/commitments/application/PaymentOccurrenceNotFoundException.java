package com.mandateguardian.commitments.application;

import java.util.UUID;

public class PaymentOccurrenceNotFoundException extends RuntimeException {

    public PaymentOccurrenceNotFoundException(UUID occurrenceId) {
        super("Payment occurrence not found: " + occurrenceId);
    }
}
