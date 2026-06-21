package ru.mkenopsia.coderunnerservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "rabbitmq.code-execution")
public class RabbitCodeExecutionProps {

    private String requestQueueName;

    private String resultQueueName;

    private String exchangeName;

    private String requestRoutingKey;

    private String resultRoutingKey;
}
