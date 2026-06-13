package ru.mkenopsia.coderunnerservice.model;

public record TestResult(
        Integer testNumber,
        String inputData,
        String expectedOutput,
        String actualOutput,
        TestStatus status
) {}
