package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.domain.CommitmentCategory;
import com.mandateguardian.commitments.domain.PaymentMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateCommitmentRequest(
        @NotBlank @Size(max = 120) String displayName,
        @NotNull CommitmentCategory category,
        @PositiveOrZero long expectedAmountMinor,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @Min(1) @Max(31) int dueDay,
        @NotNull PaymentMode paymentMode) {
}
