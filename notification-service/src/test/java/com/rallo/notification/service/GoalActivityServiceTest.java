package com.rallo.notification.service;

import com.rallo.notification.events.CheckinRecordedEvent;
import com.rallo.notification.model.GoalActivity;
import com.rallo.notification.model.NotificationType;
import com.rallo.notification.repository.GoalActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalActivityServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 6);
    private static final Clock FIXED = Clock.fixed(
            TODAY.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(72_000), ZoneOffset.UTC);

    @Mock
    private GoalActivityRepository goalActivityRepository;

    @Mock
    private NotificationService notificationService;

    private GoalActivityService service;

    @BeforeEach
    void setUp() {
        service = new GoalActivityService(goalActivityRepository, notificationService, FIXED);
    }

    private GoalActivity activity(String goalId, LocalDate lastCheckin, int streak) {
        GoalActivity a = new GoalActivity();
        a.setGoalId(goalId);
        a.setUserId("user-1");
        a.setGoalTitle("Gym");
        a.setFrequency("DAILY");
        a.setLastCheckinDate(lastCheckin);
        a.setCurrentStreak(streak);
        a.setUpdatedAt(Instant.now(FIXED));
        return a;
    }

    @Test
    void recordCheckinCreatesReadModelEntry() {
        when(goalActivityRepository.findById("goal-1")).thenReturn(Optional.empty());
        when(goalActivityRepository.save(any(GoalActivity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.recordCheckin(new CheckinRecordedEvent(
                "user-1", "goal-1", "Gym", "DAILY", TODAY, 3));

        ArgumentCaptor<GoalActivity> saved = ArgumentCaptor.forClass(GoalActivity.class);
        verify(goalActivityRepository).save(saved.capture());
        assertThat(saved.getValue().getGoalId()).isEqualTo("goal-1");
        assertThat(saved.getValue().getLastCheckinDate()).isEqualTo(TODAY);
        assertThat(saved.getValue().getCurrentStreak()).isEqualTo(3);
        assertThat(saved.getValue().getFrequency()).isEqualTo("DAILY");
    }

    @Test
    void recordCheckinDoesNotRegressLastCheckinDateOnBackfill() {
        GoalActivity existing = activity("goal-1", TODAY, 5);
        when(goalActivityRepository.findById("goal-1")).thenReturn(Optional.of(existing));
        when(goalActivityRepository.save(any(GoalActivity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.recordCheckin(new CheckinRecordedEvent(
                "user-1", "goal-1", "Gym", "DAILY", TODAY.minusDays(3), 5));

        assertThat(existing.getLastCheckinDate()).isEqualTo(TODAY);
    }

    @Test
    void sweepRemindsDailyGoalsLastCheckedInYesterday() {
        GoalActivity atRisk = activity("goal-1", TODAY.minusDays(1), 6);
        when(goalActivityRepository.findByFrequencyAndLastCheckinDate("DAILY", TODAY.minusDays(1)))
                .thenReturn(List.of(atRisk));
        when(goalActivityRepository.findByLastCheckinDateBeforeAndCurrentStreakGreaterThan(
                TODAY.minusDays(1), 0)).thenReturn(List.of());

        service.nightlySweep();

        verify(notificationService).record(
                eq("user-1"), eq(NotificationType.STREAK_REMINDER), contains("6-day streak"));
        assertThat(atRisk.getLastReminderDate()).isEqualTo(TODAY);
    }

    @Test
    void sweepDoesNotRemindTwiceOnTheSameDay() {
        GoalActivity alreadyReminded = activity("goal-1", TODAY.minusDays(1), 6);
        alreadyReminded.setLastReminderDate(TODAY);
        when(goalActivityRepository.findByFrequencyAndLastCheckinDate("DAILY", TODAY.minusDays(1)))
                .thenReturn(List.of(alreadyReminded));
        when(goalActivityRepository.findByLastCheckinDateBeforeAndCurrentStreakGreaterThan(
                TODAY.minusDays(1), 0)).thenReturn(List.of());

        service.nightlySweep();

        verify(notificationService, never()).record(anyString(), any(), anyString());
    }

    @Test
    void sweepNotifiesAndZeroesBrokenStreaks() {
        GoalActivity lapsed = activity("goal-2", TODAY.minusDays(3), 12);
        when(goalActivityRepository.findByFrequencyAndLastCheckinDate("DAILY", TODAY.minusDays(1)))
                .thenReturn(List.of());
        when(goalActivityRepository.findByLastCheckinDateBeforeAndCurrentStreakGreaterThan(
                TODAY.minusDays(1), 0)).thenReturn(List.of(lapsed));

        service.nightlySweep();

        verify(notificationService).record(
                eq("user-1"), eq(NotificationType.STREAK_BROKEN), contains("12-day streak"));
        assertThat(lapsed.getCurrentStreak()).isZero();
    }
}
