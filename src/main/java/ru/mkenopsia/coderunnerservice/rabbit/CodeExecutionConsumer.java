package ru.mkenopsia.coderunnerservice.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.dto.TestRunRequest;
import ru.mkenopsia.coderunnerservice.service.CodeTestingService;
import ru.mkenopsia.coderunnerservice.service.TestService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionConsumer {

    private final CodeTestingService codeTestingService;
    private final TestService testService;

    @RabbitListener(queues = "${rabbitmq.code-execution.queue}")
    public void listen(CodeExecutionRequest request) {
        log.info("Получен запрос на тестирование {}", request);

        var testEntities = testService.findByTaskId(request.taskId());
        if (testEntities.isEmpty()) {
            log.warn("Не найдены тесты для taskId={}", request.taskId());
            return;
        }

        var testCases = testEntities.stream()
                .map(t -> new TestRunRequest.TestCase(t.getInputData(), t.getExpectedOutput()))
                .toList();

        var testRunRequest = new TestRunRequest(
                request.code(), request.language(), testCases, request.packageId()
        );

        var results = codeTestingService.runTests(testRunRequest);
        log.info("Результаты тестирования для taskId={}: {}", request.taskId(), results);
    }
}
