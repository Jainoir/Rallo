package com.rallo.notification.consumer;

import com.rallo.notification.events.CheckinRecordedEvent;
import com.rallo.notification.events.StreakBrokenEvent;
import com.rallo.notification.model.NotificationType;
import com.rallo.notification.service.GoalActivityService;
import com.rallo.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CheckinEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private GoalActivityService goalActivityService;

    @InjectMocks
    private CheckinEventConsumer consumer;

    private CheckinRecordedEvent event(int streak) {
        return new CheckinRecordedEvent("user-1", "goal-1", "Gym", "DAILY", LocalDate.now(), streak);
    }

    @Test
    void everyCheckinUpdatesTheReadModel() {
        consumer.onCheckinRecorded(event(3));

        verify(goalActivityService).recordCheckin(event(3));
        verifyNoInteractions(notificationService);
    }

    @Test
    void milestoneStreakCreatesNotification() {
        consumer.onCheckinRecorded(event(7));

        verify(notificationService).record(
                eq("user-1"), eq(NotificationType.STREAK_MILESTONE), contains("7-day streak"));
    }

    @Test
    void brokenStreakEventCreatesNotification() {
        consumer.onStreakBroken(new StreakBrokenEvent("user-1", "goal-1", "Gym", 12));

        verify(notificationService).record(
                eq("user-1"), eq(NotificationType.STREAK_BROKEN), anyString());
    }
}
