package ru.mkenopsia.coderunnerservice.dto;

import java.util.UUID;

public record CodeExecutionRequest(
        String code,
        String language,
        UUID taskId,
        UUID packageId,
        String correlationId
) {}
