package ru.mkenopsia.coderunnerservice.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitConfig {

    private final RabbitCodeExecutionProps props;

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        var mapper = new ObjectMapper();
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public Queue requestQueue() {
        return QueueBuilder.durable(props.getRequestQueueName()).build();
    }

    @Bean
    public Queue resultQueue() {
        return QueueBuilder.durable(props.getResultQueueName()).build();
    }

    @Bean
    public DirectExchange exchange() {
        return ExchangeBuilder.directExchange(props.getExchangeName())
                .durable(true)
                .build();
    }

    @Bean
    public Binding requestQueueBinding(@Qualifier("requestQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(props.getRequestRoutingKey());
    }

    @Bean
    public Binding resultQueueBinding(@Qualifier("resultQueue") Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(props.getResultRoutingKey());
    }
}
