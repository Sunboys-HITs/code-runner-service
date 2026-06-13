package ru.mkenopsia.coderunnerservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "rabbitmq.code-execution")
public class RabbitCodeExecutionProps {

    private String successQueueName;

    private String failedQueueName;

    private String exchangeName;

    private String queue;

    private String queueRoutingKey;

    private String successRoutingKey;

    private String failedRoutingKey;
}
