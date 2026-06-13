package ru.mkenopsia.coderunnerservice.dto;

import java.util.List;
import java.util.UUID;

public record TestRunRequest(
        String code,
        String language,
        List<TestCase> testCases,
        UUID packageId
) {
    public record TestCase(String input, String expectedOutput) {}
}
