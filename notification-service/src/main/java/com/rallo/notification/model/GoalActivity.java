package com.rallo.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Read model built from check-in events. Lets the nightly reminder job find
 * at-risk and lapsed streaks without calling the check-in service — the
 * notification service stays decoupled and owns the data it queries.
 */
@Entity
@Table(name = "goal_activity")
@Getter
@Setter
@NoArgsConstructor
public class GoalActivity {

    /** The goal id from the check-in service — natural key of this read model. */
    @Id
    private String goalId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 200)
    private String goalTitle;

    /** DAILY or WEEKLY, mirrored from the event as a string to avoid coupling. */
    @Column(nullable = false, length = 20)
    private String frequency;

    @Column(nullable = false)
    private LocalDate lastCheckinDate;

    @Column(nullable = false)
    private int currentStreak;

    /** Day the last at-risk reminder was sent — prevents duplicate reminders. */
    private LocalDate lastReminderDate;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}
