package com.mandateguardian.commitments.api;

import com.mandateguardian.commitments.application.GetCommitmentOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/overview")
class CommitmentOverviewController {

    private final GetCommitmentOverviewService getCommitmentOverviewService;

    CommitmentOverviewController(GetCommitmentOverviewService getCommitmentOverviewService) {
        this.getCommitmentOverviewService = getCommitmentOverviewService;
    }

    @GetMapping
    CommitmentOverviewResponse getOverview(@RequestParam(defaultValue = "30") int days) {
        return CommitmentOverviewResponse.from(getCommitmentOverviewService.get(days));
    }
}
