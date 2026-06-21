package ru.mkenopsia.coderunnerservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.dto.TestRunRequest;
import ru.mkenopsia.coderunnerservice.model.CodeExecutionResult;
import ru.mkenopsia.coderunnerservice.model.FailedTest;
import ru.mkenopsia.coderunnerservice.model.TestResult;
import ru.mkenopsia.coderunnerservice.model.TestStatus;

import ru.mkenopsia.coderunnerservice.model.TestEntity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerCodeTestingService implements CodeTestingService {

    private final CodeExecutionService codeExecutionService;
    private final TestService testService;
    private final MeterRegistry meterRegistry;

    @Override
    public List<TestResult> runTests(TestRunRequest request) {
        var testCases = request.testCases();
        if (testCases == null || testCases.isEmpty()) {
            return List.of();
        }

        var results = new ArrayList<TestResult>(testCases.size());

        for (int i = 0; i < testCases.size(); i++) {
            var testCase = testCases.get(i);
            results.add(runSingleTest(request.code(), request.language(), testCase, i + 1));
        }

        return results;
    }

    @Override
    public CodeExecutionResult executeTests(CodeExecutionRequest request) {
        var testEntities = testService.findByTaskId(request.taskId());
        if (testEntities.isEmpty()) {
            log.warn("Не найдены тесты для taskId={}", request.taskId());
            return CodeExecutionResult.builder()
                    .passedTests(0)
                    .failedTestsCount(0)
                    .failedTests(List.of())
                    .correlationId(request.correlationId())
                    .packageId(request.packageId())
                    .build();
        }

        var testCases = testEntities.stream()
                .map(t -> new TestRunRequest.TestCase(t.getInputData(), t.getExpectedOutput()))
                .toList();

        var testRunRequest = new TestRunRequest(
                request.code(), request.language(), testCases, request.packageId()
        );

        var results = runTests(testRunRequest);
        log.info("Результаты тестирования для taskId={}: {}", request.taskId(), results);

        return aggregateResults(results, testEntities, request);
    }

    private CodeExecutionResult aggregateResults(
            List<TestResult> results,
            List<TestEntity> testEntities,
            CodeExecutionRequest request
    ) {
        int passed = 0;
        var failedTests = new ArrayList<FailedTest>();

        for (int i = 0; i < results.size(); i++) {
            var testResult = results.get(i);
            if (testResult.status() == TestStatus.PASSED) {
                passed++;
            } else {
                var entity = testEntities.get(i);
                var reason = buildFailureReason(testResult);
                failedTests.add(new FailedTest(entity.getTestId(), reason));
            }
        }

        var lang = request.language();
        meterRegistry.counter("tests.passed", "language", lang).increment(passed);
        meterRegistry.counter("tests.failed", "language", lang).increment(failedTests.size());

        return CodeExecutionResult.builder()
                .passedTests(passed)
                .failedTestsCount(failedTests.size())
                .failedTests(failedTests)
                .correlationId(request.correlationId())
                .packageId(request.packageId())
                .build();
    }

    private String buildFailureReason(TestResult testResult) {
        var actual = testResult.actualOutput();
        if (actual.startsWith("COMPILATION ERROR:") || actual.startsWith("RUNTIME ERROR:")) {
            return actual;
        }
        return "Expected: " + testResult.expectedOutput() + ", but got: " + actual;
    }

    private TestResult runSingleTest(String code, String language, TestRunRequest.TestCase testCase, int testNumber) {
        try {
            String actualOutput = codeExecutionService.executeCode(code, language, testCase.input());

            if (actualOutput.startsWith("COMPILATION ERROR:") || actualOutput.startsWith("RUNTIME ERROR:")) {
                return new TestResult(
                        testNumber,
                        testCase.input(),
                        testCase.expectedOutput(),
                        actualOutput,
                        TestStatus.FAILED
                );
            }

            String cleanOutput = extractActualOutput(actualOutput);
            boolean passed = cleanOutput.equals(testCase.expectedOutput().trim());

            return new TestResult(
                    testNumber,
                    testCase.input(),
                    testCase.expectedOutput(),
                    cleanOutput,
                    passed ? TestStatus.PASSED : TestStatus.FAILED
            );
        } catch (Exception e) {
            log.error("Ошибка при выполнении теста #{}", testNumber, e);
            return new TestResult(
                    testNumber,
                    testCase.input(),
                    testCase.expectedOutput(),
                    "EXECUTION ERROR: " + e.getMessage(),
                    TestStatus.FAILED
            );
        }
    }

    private String extractActualOutput(String classifiedResult) {
        int warnIdx = classifiedResult.indexOf("\n[WARN] ");
        if (warnIdx != -1) {
            return classifiedResult.substring(0, warnIdx).trim();
        }
        return classifiedResult.trim();
    }
}
