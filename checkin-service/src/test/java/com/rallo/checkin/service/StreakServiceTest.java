package com.rallo.checkin.service;

import com.rallo.checkin.model.Checkin;
import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.repository.CheckinRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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

    @Test
    void returnsZeroWhenNoCheckins() {
        givenCheckinDates();

        assertThat(streakService.currentStreak(GOAL_ID, Frequency.DAILY)).isZero();
    }

    @Test
    void countsSingleCheckinToday() {
        givenCheckinDates(LocalDate.now());

        assertThat(streakService.currentStreak(GOAL_ID, Frequency.DAILY)).isEqualTo(1);
    }

    @Test
    void countsConsecutiveDailyCheckins() {
        LocalDate today = LocalDate.now();
        givenCheckinDates(today, today.minusDays(1), today.minusDays(2));

        assertThat(streakService.currentStreak(GOAL_ID, Frequency.DAILY)).isEqualTo(3);
    }

    @Test
    void streakSurvivesWhenTodayNotYetCheckedIn() {
        // Checked in yesterday and the day before — today is still open, streak holds.
        LocalDate yesterday = LocalDate.now().minusDays(1);
        givenCheckinDates(yesterday, yesterday.minusDays(1));

        assertThat(streakService.currentStreak(GOAL_ID, Frequency.DAILY)).isEqualTo(2);
    }

    @Test
    void gapBreaksStreak() {
        LocalDate today = LocalDate.now();
        givenCheckinDates(today, today.minusDays(1), today.minusDays(4), today.minusDays(5));

        assertThat(streakService.currentStreak(GOAL_ID, Frequency.DAILY)).isEqualTo(2);
    }

    @Test
    void returnsZeroWhenLastCheckinTooOld() {
        givenCheckinDates(LocalDate.now().minusDays(2));

        assertThat(streakService.currentStreak(GOAL_ID, Frequency.DAILY)).isZero();
    }

    private void givenCheckinDates(LocalDate... dates) {
        List<Checkin> checkins = Arrays.stream(dates)
                .map(date -> {
                    Checkin checkin = new Checkin();
                    checkin.setCheckinDate(date);
                    return checkin;
                })
                .toList();
        when(checkinRepository.findByGoalIdOrderByCheckinDateDesc(GOAL_ID)).thenReturn(checkins);
    }
}
