package com.rallo.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    /**
     * Runs nightly at 20:00 server time.
     *
     * TODO: query users whose last check-in was yesterday and whose streak
     *       would break if they don't check in today, then send reminders.
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void sendStreakReminders() {
        log.info("Nightly streak reminder job started");
        // TODO: implement reminder logic
    }
}
