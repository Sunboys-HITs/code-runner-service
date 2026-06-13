package ru.mkenopsia.coderunnerservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final RabbitCodeExecutionProps props;

    @Bean
    public Queue codeExecutionQueue() {
        return QueueBuilder.durable(props.getQueue())
                .build();
    }

    @Bean
    public Queue successExecutionsQueue() {
        return QueueBuilder.durable(props.getSuccessQueueName()).build();
    }

    @Bean
    public Queue failedExecutionsQueue() {
        return QueueBuilder.durable(props.getFailedQueueName()).build();
    }

    @Bean
    public DirectExchange exchange() {
        return ExchangeBuilder.directExchange(props.getExchangeName())
                .durable(true)
                .build();
    }

    @Bean
    public Binding successQueueBinding(@Qualifier("successExecutionsQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(props.getSuccessRoutingKey());
    }

    @Bean
    public Binding failedQueueBinding(@Qualifier("failedExecutionsQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(props.getFailedRoutingKey());
    }

    @Bean
    public Binding codeExecutionQueueBinding(@Qualifier("codeExecutionQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(props.getQueueRoutingKey());
    }
}
