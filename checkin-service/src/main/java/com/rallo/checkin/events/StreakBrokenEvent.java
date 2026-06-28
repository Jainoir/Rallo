package com.rallo.checkin.events;

public record StreakBrokenEvent(
        String userId,
        String goalId,
        String goalTitle,
        int previousStreak
) {}
