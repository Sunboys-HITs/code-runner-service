package ru.mkenopsia.coderunnerservice.model;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
public record CodeExecutionResult(
        int passedTests,
        int failedTestsCount,
        List<FailedTest> failedTests,
        String correlationId,
        UUID packageId
) {
    public CodeExecutionResult {
        if (failedTests == null) failedTests = new ArrayList<>();
        if (correlationId == null) correlationId = "";
    }
}

