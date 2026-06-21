package ru.mkenopsia.coderunnerservice.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public void sendResult(CodeExecutionResult result) {
        var routingKey = result.failedTestsCount() > 0
                ? props.getFailedRoutingKey()
                : props.getSuccessRoutingKey();

        try {
            String json = objectMapper.writeValueAsString(result);
            rabbitTemplate.convertAndSend(props.getExchangeName(), routingKey, json);
            log.info("Результат отправлен в exchange={}, routingKey={}: {}", props.getExchangeName(), routingKey, json);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации CodeExecutionResult", e);
            throw new RuntimeException("Failed to serialize CodeExecutionResult", e);
        }
    }
}
