package com.campus.dispatch.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTICE_QUEUE = "notice.queue";
    public static final String NOTICE_EXCHANGE = "notice.exchange";
    public static final String NOTICE_ROUTING_KEY = "notice.routing";

    @Bean
    public Queue noticeQueue() {
        return new Queue(NOTICE_QUEUE, true);
    }

    @Bean
    public DirectExchange noticeExchange() {
        return new DirectExchange(NOTICE_EXCHANGE, true, false);
    }

    @Bean
    public Binding noticeBinding() {
        return BindingBuilder.bind(noticeQueue()).to(noticeExchange()).with(NOTICE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}