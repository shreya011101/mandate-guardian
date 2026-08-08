package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.ApplyPaymentOccurrenceActionCommand;
import com.mandateguardian.commitments.application.ApplyPaymentOccurrenceActionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-occurrences")
class PaymentOccurrenceController {

    private final ApplyPaymentOccurrenceActionService applyPaymentOccurrenceActionService;

    PaymentOccurrenceController(ApplyPaymentOccurrenceActionService applyPaymentOccurrenceActionService) {
        this.applyPaymentOccurrenceActionService = applyPaymentOccurrenceActionService;
    }

    @PostMapping("/{occurrenceId}/actions")
    AppliedPaymentOccurrenceActionResponse applyAction(
            @PathVariable UUID occurrenceId,
            @Valid @RequestBody ApplyPaymentOccurrenceActionRequest request) {
        return AppliedPaymentOccurrenceActionResponse.from(applyPaymentOccurrenceActionService.apply(
                new ApplyPaymentOccurrenceActionCommand(
                        occurrenceId,
                        request.action(),
                        request.postponedDueDate())));
    }
}
