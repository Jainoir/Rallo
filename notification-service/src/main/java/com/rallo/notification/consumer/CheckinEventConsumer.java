package com.rallo.notification.consumer;

import com.rallo.notification.config.RabbitMQConfig;
import com.rallo.notification.events.CheckinRecordedEvent;
import com.rallo.notification.events.StreakBrokenEvent;
import com.rallo.notification.model.NotificationType;
import com.rallo.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckinEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.CHECKIN_RECORDED)
    public void onCheckinRecorded(CheckinRecordedEvent event) {
        log.info("Check-in recorded — user={}, goal='{}', streak={}",
                event.userId(), event.goalTitle(), event.currentStreak());

        if (event.currentStreak() > 0 && event.currentStreak() % 7 == 0) {
            notificationService.record(event.userId(), NotificationType.STREAK_MILESTONE,
                    "Milestone! You're on a %d-day streak for '%s'. Keep it going!"
                            .formatted(event.currentStreak(), event.goalTitle()));
            // TODO: send push/email in addition to the in-app notification
        }
    }

    @RabbitListener(queues = RabbitMQConfig.STREAK_BROKEN)
    public void onStreakBroken(StreakBrokenEvent event) {
        log.info("Streak broken — user={}, goal='{}', previous streak={}",
                event.userId(), event.goalTitle(), event.previousStreak());

        notificationService.record(event.userId(), NotificationType.STREAK_BROKEN,
                "Your %d-day streak on '%s' was broken. Check in today to start a new one!"
                        .formatted(event.previousStreak(), event.goalTitle()));
        // TODO: send push/email in addition to the in-app notification
    }
}
