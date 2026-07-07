package com.rallo.notification.service;

import com.rallo.notification.events.CheckinRecordedEvent;
import com.rallo.notification.model.GoalActivity;
import com.rallo.notification.model.NotificationType;
import com.rallo.notification.repository.GoalActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalActivityService {

    private static final String DAILY = "DAILY";

    private final GoalActivityRepository goalActivityRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    /** Keeps the read model current; called for every consumed check-in event. */
    @Transactional
    public void recordCheckin(CheckinRecordedEvent event) {
        GoalActivity activity = goalActivityRepository.findById(event.goalId())
                .orElseGet(() -> {
                    GoalActivity created = new GoalActivity();
                    created.setGoalId(event.goalId());
                    return created;
                });

        activity.setUserId(event.userId());
        activity.setGoalTitle(event.goalTitle());
        activity.setFrequency(event.frequency() == null ? DAILY : event.frequency());
        if (activity.getLastCheckinDate() == null
                || event.checkinDate().isAfter(activity.getLastCheckinDate())) {
            activity.setLastCheckinDate(event.checkinDate());
        }
        activity.setCurrentStreak(event.currentStreak());
        activity.setUpdatedAt(Instant.now(clock));
        goalActivityRepository.save(activity);
    }

    /**
     * Nightly sweep:
     *  - daily goals last checked in yesterday → streak at risk → reminder
     *    (at most one per goal per day)
     *  - any goal with an older last check-in and a positive streak → the
     *    streak is broken → notify once and zero the read-model streak
     * Weekly goals are skipped for at-risk reminders — their period only
     * closes at the end of the ISO week.
     */
    @Transactional
    public void nightlySweep() {
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);

        List<GoalActivity> atRisk = goalActivityRepository
                .findByFrequencyAndLastCheckinDate(DAILY, yesterday);
        for (GoalActivity activity : atRisk) {
            if (activity.getCurrentStreak() < 1 || today.equals(activity.getLastReminderDate())) {
                continue;
            }
            notificationService.record(activity.getUserId(), NotificationType.STREAK_REMINDER,
                    "Your %d-day streak on '%s' is at risk — check in today to keep it alive!"
                            .formatted(activity.getCurrentStreak(), activity.getGoalTitle()));
            activity.setLastReminderDate(today);
        }

        List<GoalActivity> lapsed = goalActivityRepository
                .findByLastCheckinDateBeforeAndCurrentStreakGreaterThan(yesterday, 0);
        for (GoalActivity activity : lapsed) {
            notificationService.record(activity.getUserId(), NotificationType.STREAK_BROKEN,
                    "Your %d-day streak on '%s' was broken. Check in today to start a new one!"
                            .formatted(activity.getCurrentStreak(), activity.getGoalTitle()));
            activity.setCurrentStreak(0);
        }

        log.info("Nightly sweep done — {} reminders, {} broken streaks", atRisk.size(), lapsed.size());
    }
}
