package ru.mkenopsia.coderunnerservice.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.service.CodeExecutionService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionConsumer {

    private final CodeExecutionService codeExecutionService;

    @RabbitListener(queues = "${rabbitmq.code-execution.queue}")
    public void listen(CodeExecutionRequest request) {
        log.info("Получен запрос на тестирование {}", request);
        codeExecutionService.execute(request);
    }
}
