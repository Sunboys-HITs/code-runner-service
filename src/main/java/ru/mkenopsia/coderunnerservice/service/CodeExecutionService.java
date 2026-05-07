package ru.mkenopsia.coderunnerservice.service;

import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;

public interface CodeExecutionService {

    void execute(CodeExecutionRequest request);
}
