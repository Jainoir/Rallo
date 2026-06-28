package com.rallo.notification.events;

public record StreakBrokenEvent(
        String userId,
        String goalId,
        String goalTitle,
        int previousStreak
) {}
