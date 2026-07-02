package com.rallo.notification.service;

import com.rallo.notification.dto.NotificationResponse;
import com.rallo.notification.model.Notification;
import com.rallo.notification.model.NotificationType;
import com.rallo.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String USER_ID = "user-1";

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void recordPersistsUnreadNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.record(USER_ID, NotificationType.STREAK_MILESTONE, "7-day streak!");

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.STREAK_MILESTONE);
        assertThat(saved.getValue().getMessage()).isEqualTo("7-day streak!");
        assertThat(saved.getValue().isRead()).isFalse();
    }

    @Test
    void listForUserMapsToResponses() {
        Notification notification = new Notification();
        notification.setUserId(USER_ID);
        notification.setType(NotificationType.STREAK_BROKEN);
        notification.setMessage("Streak broken");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.listForUser(USER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo(NotificationType.STREAK_BROKEN);
        assertThat(responses.get(0).message()).isEqualTo("Streak broken");
    }

    @Test
    void markReadRejectsNotificationOwnedByAnotherUser() {
        when(notificationRepository.findByIdAndUserId("notif-1", USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(USER_ID, "notif-1"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void markReadFlagsNotification() {
        Notification notification = new Notification();
        notification.setUserId(USER_ID);
        notification.setType(NotificationType.STREAK_MILESTONE);
        notification.setMessage("Milestone");
        when(notificationRepository.findByIdAndUserId("notif-1", USER_ID))
                .thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markRead(USER_ID, "notif-1");

        assertThat(notification.isRead()).isTrue();
        assertThat(response.read()).isTrue();
    }
}
