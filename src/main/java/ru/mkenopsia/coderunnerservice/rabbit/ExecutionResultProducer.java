package ru.mkenopsia.coderunnerservice.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import ru.mkenopsia.coderunnerservice.config.RabbitCodeExecutionProps;
import ru.mkenopsia.coderunnerservice.model.CodeExecutionResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionResultProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitCodeExecutionProps props;
    private final MeterRegistry meterRegistry;

    public void sendResult(CodeExecutionResult result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            rabbitTemplate.convertAndSend(props.getExchangeName(), props.getResultRoutingKey(), json);
            log.info("Результат отправлен в exchange={}, routingKey={}: {}", props.getExchangeName(), props.getResultRoutingKey(), json);
            meterRegistry.counter("producer.results.published").increment();
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации CodeExecutionResult", e);
            meterRegistry.counter("producer.results.serialization.failures").increment();
            throw new RuntimeException("Failed to serialize CodeExecutionResult", e);
        }
    }
}
