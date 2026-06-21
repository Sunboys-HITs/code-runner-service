package ru.mkenopsia.coderunnerservice.rabbit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.dto.CodeExecutionRequest;
import ru.mkenopsia.coderunnerservice.service.CodeTestingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionConsumer {

    private final CodeTestingService codeTestingService;
    private final ExecutionResultProducer resultProducer;
    private final MeterRegistry meterRegistry;

    @RabbitListener(queues = "${rabbitmq.code-execution.request-queue-name}")
    public void listen(CodeExecutionRequest request) {
        log.info("Получен запрос на тестирование {}", request);

        var lang = request.language();
        meterRegistry.counter("consumer.messages.received", "language", lang).increment();

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            var result = codeTestingService.executeTests(request);
            resultProducer.sendResult(result);
        } catch (Exception e) {
            meterRegistry.counter("consumer.messages.failed", "language", lang).increment();
            log.error("Ошибка обработки сообщения", e);
            throw e;
        } finally {
            sample.stop(meterRegistry.timer("consumer.messages.processing.time", "language", lang));
        }
    }
}
