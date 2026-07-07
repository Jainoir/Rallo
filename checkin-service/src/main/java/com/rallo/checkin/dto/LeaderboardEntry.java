package com.rallo.checkin.dto;

public record LeaderboardEntry(
        String userId,
        String username,
        int bestStreak
) {}
