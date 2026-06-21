package ru.mkenopsia.coderunnerservice.service;

import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.dto.TestRunRequest;
import ru.mkenopsia.coderunnerservice.model.CodeExecutionResult;
import ru.mkenopsia.coderunnerservice.model.TestResult;

import java.util.List;

public interface CodeTestingService {

    List<TestResult> runTests(TestRunRequest request);

    CodeExecutionResult executeTests(CodeExecutionRequest request);
}
