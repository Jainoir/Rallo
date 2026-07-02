package com.rallo.notification.consumer;

import com.rallo.notification.events.CheckinRecordedEvent;
import com.rallo.notification.events.StreakBrokenEvent;
import com.rallo.notification.model.NotificationType;
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

    @InjectMocks
    private CheckinEventConsumer consumer;

    @Test
    void milestoneStreakCreatesNotification() {
        consumer.onCheckinRecorded(
                new CheckinRecordedEvent("user-1", "goal-1", "Gym", LocalDate.now(), 7));

        verify(notificationService).record(
                eq("user-1"), eq(NotificationType.STREAK_MILESTONE), contains("7-day streak"));
    }

    @Test
    void nonMilestoneStreakCreatesNoNotification() {
        consumer.onCheckinRecorded(
                new CheckinRecordedEvent("user-1", "goal-1", "Gym", LocalDate.now(), 3));

        verifyNoInteractions(notificationService);
    }

    @Test
    void brokenStreakCreatesNotification() {
        consumer.onStreakBroken(
                new StreakBrokenEvent("user-1", "goal-1", "Gym", 12));

        verify(notificationService).record(
                eq("user-1"), eq(NotificationType.STREAK_BROKEN), anyString());
    }
}
