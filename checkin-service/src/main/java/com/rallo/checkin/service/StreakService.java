package com.rallo.checkin.service;

import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.CheckinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final CheckinRepository checkinRepository;

    /**
     * Number of consecutive periods the user has checked in, evaluated in the
     * user's timezone so "today" rolls over at their midnight, not the server's.
     *
     * DAILY: consecutive days ending today or yesterday (today still open).
     * WEEKLY: consecutive ISO weeks (Mon–Sun) meeting the goal's target
     * days-per-week; the current week never breaks the streak while it is
     * still in progress — it only counts once the target is met.
     */
    public int currentStreak(Goal goal, ZoneId zone) {
        List<LocalDate> dates = checkinRepository
                .findByGoalIdOrderByCheckinDateDesc(goal.getId())
                .stream()
                .map(c -> c.getCheckinDate())
                .toList();

        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now(zone);
        return goal.getFrequency() == Frequency.DAILY
                ? dailyStreak(dates, today)
                : weeklyStreak(dates, today, goal.getTargetDaysPerWeek());
    }

    private int dailyStreak(List<LocalDate> datesDesc, LocalDate today) {
        int streak = 0;
        LocalDate cursor = today;

        for (LocalDate date : datesDesc) {
            if (date.equals(cursor) || date.equals(cursor.minusDays(1))) {
                streak++;
                cursor = date;
            } else {
                break;
            }
        }
        return streak;
    }

    private int weeklyStreak(List<LocalDate> datesDesc, LocalDate today, Integer targetDaysPerWeek) {
        int target = targetDaysPerWeek == null ? 1 : targetDaysPerWeek;

        Map<LocalDate, Long> checkinsPerWeek = datesDesc.stream()
                .collect(Collectors.groupingBy(
                        date -> date.with(DayOfWeek.MONDAY), Collectors.counting()));

        LocalDate currentWeek = today.with(DayOfWeek.MONDAY);
        int streak = 0;

        // The running week counts if the target is already met; if not, it is
        // still open, so it neither counts nor breaks the streak.
        LocalDate week = currentWeek;
        if (checkinsPerWeek.getOrDefault(week, 0L) >= target) {
            streak++;
        }
        week = week.minusWeeks(1);

        while (checkinsPerWeek.getOrDefault(week, 0L) >= target) {
            streak++;
            week = week.minusWeeks(1);
        }
        return streak;
    }
}
