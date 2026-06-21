package ru.mkenopsia.coderunnerservice.model;

import java.util.UUID;

public record FailedTest(UUID testId, String reason) {}
