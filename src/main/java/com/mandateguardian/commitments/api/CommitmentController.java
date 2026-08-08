package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.CreateMonthlyCommitmentCommand;
import com.mandateguardian.commitments.application.CreateMonthlyCommitmentService;
import com.mandateguardian.commitments.application.ListMonthlyCommitmentsService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.YearMonth;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commitments")
class CommitmentController {

    private final CreateMonthlyCommitmentService createMonthlyCommitmentService;
    private final ListMonthlyCommitmentsService listMonthlyCommitmentsService;

    CommitmentController(
            CreateMonthlyCommitmentService createMonthlyCommitmentService,
            ListMonthlyCommitmentsService listMonthlyCommitmentsService) {
        this.createMonthlyCommitmentService = createMonthlyCommitmentService;
        this.listMonthlyCommitmentsService = listMonthlyCommitmentsService;
    }

    @GetMapping
    java.util.List<MonthlyCommitmentResponse> list(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return listMonthlyCommitmentsService.list(month).stream()
                .map(MonthlyCommitmentResponse::from)
                .toList();
    }

    @PostMapping
    ResponseEntity<CreatedCommitmentResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateCommitmentRequest request) {
        var result = createMonthlyCommitmentService.create(new CreateMonthlyCommitmentCommand(
                request.displayName(),
                request.category(),
                request.expectedAmountMinor(),
                request.currency(),
                request.dueDay(),
                request.paymentMode(),
                idempotencyKey));
        var response = CreatedCommitmentResponse.from(result);
        return ResponseEntity.created(URI.create("/api/v1/commitments/" + response.commitmentId()))
                .body(response);
    }
}
