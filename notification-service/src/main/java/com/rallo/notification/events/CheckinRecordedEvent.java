package com.rallo.notification.events;

import java.time.LocalDate;

public record CheckinRecordedEvent(
        String userId,
        String goalId,
        String goalTitle,
        LocalDate checkinDate,
        int currentStreak
) {}
