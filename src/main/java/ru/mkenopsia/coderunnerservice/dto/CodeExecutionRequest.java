package ru.mkenopsia.coderunnerservice.dto;

import java.util.UUID;

public record CodeExecutionRequest(
        String code,
        String language,
        String taskId,
        UUID packageId
) {}
