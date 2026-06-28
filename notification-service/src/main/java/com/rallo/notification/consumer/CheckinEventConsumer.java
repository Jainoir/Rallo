package com.rallo.notification.consumer;

import com.rallo.notification.config.RabbitMQConfig;
import com.rallo.notification.events.CheckinRecordedEvent;
import com.rallo.notification.events.StreakBrokenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckinEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.CHECKIN_RECORDED)
    public void onCheckinRecorded(CheckinRecordedEvent event) {
        log.info("Check-in recorded — user={}, goal='{}', streak={}",
                event.userId(), event.goalTitle(), event.currentStreak());

        if (event.currentStreak() > 0 && event.currentStreak() % 7 == 0) {
            log.info("Milestone! User {} has a {}-day streak on '{}'",
                    event.userId(), event.currentStreak(), event.goalTitle());
            // TODO: persist notification record and send push/email
        }
    }

    @RabbitListener(queues = RabbitMQConfig.STREAK_BROKEN)
    public void onStreakBroken(StreakBrokenEvent event) {
        log.info("Streak broken — user={}, goal='{}', previous streak={}",
                event.userId(), event.goalTitle(), event.previousStreak());
        // TODO: persist notification record and send push/email
    }
}
