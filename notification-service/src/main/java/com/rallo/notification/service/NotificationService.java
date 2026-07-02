package com.rallo.notification.service;

import com.rallo.notification.dto.NotificationResponse;
import com.rallo.notification.model.Notification;
import com.rallo.notification.model.NotificationType;
import com.rallo.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Called by the RabbitMQ consumers to persist an in-app notification. */
    @Transactional
    public Notification record(String userId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    public List<NotificationResponse> listForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long unreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found: " + notificationId));
        notification.setRead(true);
        return NotificationResponse.from(notification);
    }
}
