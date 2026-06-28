package com.rallo.checkin.events;

import com.rallo.checkin.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckinEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCheckinRecorded(CheckinRecordedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.CHECKIN_RECORDED, event);
    }

    public void publishStreakBroken(StreakBrokenEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.STREAK_BROKEN, event);
    }
}
