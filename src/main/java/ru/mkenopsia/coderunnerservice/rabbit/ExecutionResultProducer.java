package ru.mkenopsia.coderunnerservice.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExecutionResultProducer {

    private final RabbitTemplate rabbitTemplate;

    public void produce(String exchange, String key, String message) {
        rabbitTemplate.convertAndSend(exchange, key, message);
    }
}
