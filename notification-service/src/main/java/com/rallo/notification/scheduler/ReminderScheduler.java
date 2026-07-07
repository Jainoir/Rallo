package com.rallo.notification.scheduler;

import com.rallo.notification.service.GoalActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final GoalActivityService goalActivityService;

    /**
     * Runs nightly at 20:00 UTC: reminds users whose daily streak is at risk
     * (last check-in was yesterday) and records broken-streak notifications
     * for goals that lapsed. Backed by the goal-activity read model built
     * from check-in events — no call to the check-in service needed.
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void sendStreakReminders() {
        log.info("Nightly streak reminder job started");
        goalActivityService.nightlySweep();
    }
}
