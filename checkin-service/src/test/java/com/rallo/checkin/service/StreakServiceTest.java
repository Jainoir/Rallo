package com.rallo.checkin.service;

import com.rallo.checkin.model.Checkin;
import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.CheckinRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    private static final String GOAL_ID = "goal-1";

    @Mock
    private CheckinRepository checkinRepository;

    @InjectMocks
    private StreakService streakService;

    private Goal goal(Frequency frequency, Integer targetDaysPerWeek) {
        Goal goal = new Goal();
        goal.setId(GOAL_ID);
        goal.setUserId("user-1");
        goal.setTitle("Gym");
        goal.setFrequency(frequency);
        goal.setTargetDaysPerWeek(targetDaysPerWeek);
        return goal;
    }

    private void givenCheckinDates(LocalDate... dates) {
        List<Checkin> checkins = Arrays.stream(dates)
                .sorted((a, b) -> b.compareTo(a))
                .map(date -> {
                    Checkin checkin = new Checkin();
                    checkin.setCheckinDate(date);
                    return checkin;
                })
                .toList();
        when(checkinRepository.findByGoalIdOrderByCheckinDateDesc(GOAL_ID)).thenReturn(checkins);
    }

    private int daily() {
        return streakService.currentStreak(goal(Frequency.DAILY, null), ZoneOffset.UTC);
    }

    private int weekly(Integer target) {
        return streakService.currentStreak(goal(Frequency.WEEKLY, target), ZoneOffset.UTC);
    }

    // ── daily ────────────────────────────────────────────────────────────────

    @Test
    void dailyReturnsZeroWhenNoCheckins() {
        givenCheckinDates();
        assertThat(daily()).isZero();
    }

    @Test
    void dailyCountsConsecutiveDays() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        givenCheckinDates(today, today.minusDays(1), today.minusDays(2));
        assertThat(daily()).isEqualTo(3);
    }

    @Test
    void dailyStreakSurvivesWhenTodayNotYetCheckedIn() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        givenCheckinDates(yesterday, yesterday.minusDays(1));
        assertThat(daily()).isEqualTo(2);
    }

    @Test
    void dailyGapBreaksStreak() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        givenCheckinDates(today, today.minusDays(1), today.minusDays(4));
        assertThat(daily()).isEqualTo(2);
    }

    @Test
    void dailyReturnsZeroWhenLastCheckinTooOld() {
        givenCheckinDates(LocalDate.now(ZoneOffset.UTC).minusDays(2));
        assertThat(daily()).isZero();
    }

    @Test
    void dailyUsesCallerTimezoneForToday() {
        // At 2026-07-06 02:00 UTC it is still 2026-07-05 in Montreal (UTC-4).
        // A user who checked in on their "yesterday" (07-04) plus "today"
        // (07-05) has streak 2 in their zone regardless of the UTC date.
        LocalDate montrealToday = LocalDate.now(java.time.ZoneId.of("America/Montreal"));
        givenCheckinDates(montrealToday, montrealToday.minusDays(1));

        int streak = streakService.currentStreak(
                goal(Frequency.DAILY, null), java.time.ZoneId.of("America/Montreal"));

        assertThat(streak).isEqualTo(2);
    }

    // ── weekly ───────────────────────────────────────────────────────────────

    @Test
    void weeklyCountsConsecutiveWeeksMeetingTarget() {
        LocalDate thisMonday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
        givenCheckinDates(
                // current week: 2 check-ins, target 2 -> counts
                thisMonday, thisMonday.plusDays(1),
                // last week: 2 check-ins -> counts
                thisMonday.minusWeeks(1), thisMonday.minusWeeks(1).plusDays(2),
                // two weeks ago: 2 check-ins -> counts
                thisMonday.minusWeeks(2), thisMonday.minusWeeks(2).plusDays(3));

        assertThat(weekly(2)).isEqualTo(3);
    }

    @Test
    void weeklyInProgressWeekDoesNotBreakStreak() {
        LocalDate thisMonday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
        givenCheckinDates(
                // current week: only 1 of target 3 so far -> open, not broken
                thisMonday,
                // last week: 3 -> counts
                thisMonday.minusWeeks(1), thisMonday.minusWeeks(1).plusDays(1),
                thisMonday.minusWeeks(1).plusDays(2),
                // two weeks ago: 3 -> counts
                thisMonday.minusWeeks(2), thisMonday.minusWeeks(2).plusDays(1),
                thisMonday.minusWeeks(2).plusDays(2));

        assertThat(weekly(3)).isEqualTo(2);
    }

    @Test
    void weeklyMissedWeekBreaksStreak() {
        LocalDate thisMonday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
        givenCheckinDates(
                thisMonday,
                // gap: last week missed entirely
                thisMonday.minusWeeks(2), thisMonday.minusWeeks(3));

        assertThat(weekly(1)).isEqualTo(1);
    }

    @Test
    void weeklyBelowTargetWeekBreaksStreak() {
        LocalDate thisMonday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
        givenCheckinDates(
                // last week: only 1 of target 2 -> breaks
                thisMonday.minusWeeks(1),
                // two weeks ago: 2 -> would count if reachable
                thisMonday.minusWeeks(2), thisMonday.minusWeeks(2).plusDays(1));

        assertThat(weekly(2)).isZero();
    }

    @Test
    void weeklyDefaultsToTargetOfOne() {
        LocalDate thisMonday = LocalDate.now(ZoneOffset.UTC).with(DayOfWeek.MONDAY);
        givenCheckinDates(thisMonday, thisMonday.minusWeeks(1));

        assertThat(weekly(null)).isEqualTo(2);
    }
}
