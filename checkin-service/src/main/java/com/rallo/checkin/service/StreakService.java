package com.rallo.checkin.service;

import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.repository.CheckinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final CheckinRepository checkinRepository;

    /**
     * Returns the number of consecutive periods (days or weeks) the user
     * has checked in up to and including today.
     *
     * TODO: add grace-day logic and timezone-aware period boundaries.
     */
    public int currentStreak(String goalId, Frequency frequency) {
        List<LocalDate> dates = checkinRepository
                .findByGoalIdOrderByCheckinDateDesc(goalId)
                .stream()
                .map(c -> c.getCheckinDate())
                .toList();

        if (dates.isEmpty()) return 0;

        int streak = 0;
        LocalDate cursor = LocalDate.now();

        for (LocalDate date : dates) {
            if (frequency == Frequency.DAILY) {
                if (date.equals(cursor) || date.equals(cursor.minusDays(1))) {
                    streak++;
                    cursor = date;
                } else {
                    break;
                }
            } else {
                // WEEKLY: count distinct ISO weeks
                // TODO: implement proper week-based streak counting
                streak++;
            }
        }

        return streak;
    }
}
