package ru.mkenopsia.coderunnerservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.TestRunRequest;
import ru.mkenopsia.coderunnerservice.model.TestResult;
import ru.mkenopsia.coderunnerservice.model.TestStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerCodeTestingService implements CodeTestingService {

    private final CodeExecutionService codeExecutionService;

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
