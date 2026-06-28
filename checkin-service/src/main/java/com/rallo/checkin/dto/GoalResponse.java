package com.rallo.checkin.dto;

import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.model.Goal;

import java.time.Instant;

public record GoalResponse(
        String id,
        String title,
        String description,
        Frequency frequency,
        Integer targetDaysPerWeek,
        boolean active,
        Instant createdAt
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getFrequency(),
                goal.getTargetDaysPerWeek(),
                goal.isActive(),
                goal.getCreatedAt()
        );
    }
}
