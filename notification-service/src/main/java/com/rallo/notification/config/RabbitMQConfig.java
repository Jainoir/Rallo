package com.rallo.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE         = "rallo.exchange";
    public static final String CHECKIN_RECORDED = "checkin.recorded";
    public static final String STREAK_BROKEN    = "streak.broken";

    @Bean
    public TopicExchange ralloExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue checkinRecordedQueue() {
        return QueueBuilder.durable(CHECKIN_RECORDED).build();
    }

    @Bean
    public Queue streakBrokenQueue() {
        return QueueBuilder.durable(STREAK_BROKEN).build();
    }

    @Bean
    public Binding checkinRecordedBinding(Queue checkinRecordedQueue, TopicExchange ralloExchange) {
        return BindingBuilder.bind(checkinRecordedQueue).to(ralloExchange).with(CHECKIN_RECORDED);
    }

    @Bean
    public Binding streakBrokenBinding(Queue streakBrokenQueue, TopicExchange ralloExchange) {
        return BindingBuilder.bind(streakBrokenQueue).to(ralloExchange).with(STREAK_BROKEN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
