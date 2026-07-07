package com.rallo.checkin.events;

import java.time.LocalDate;

public record CheckinRecordedEvent(
        String userId,
        String goalId,
        String goalTitle,
        String frequency,
        LocalDate checkinDate,
        int currentStreak
) {}
