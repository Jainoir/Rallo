package com.rallo.checkin.service;

import com.rallo.checkin.dto.CreateGoalRequest;
import com.rallo.checkin.dto.GoalResponse;
import com.rallo.checkin.model.Frequency;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    private static final String USER_ID = "user-1";

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalService goalService;

    @Test
    void createAssignsOwnerAndMapsFields() {
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateGoalRequest request = new CreateGoalRequest(
                "Gym", "Lift 3x per week", Frequency.WEEKLY, 3);
        GoalResponse response = goalService.create(USER_ID, request);

        assertThat(response.title()).isEqualTo("Gym");
        assertThat(response.description()).isEqualTo("Lift 3x per week");
        assertThat(response.frequency()).isEqualTo(Frequency.WEEKLY);
        assertThat(response.targetDaysPerWeek()).isEqualTo(3);
        assertThat(response.active()).isTrue();
    }

    @Test
    void getThrowsWhenGoalNotOwnedByUser() {
        when(goalRepository.findByIdAndUserId("goal-1", USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.get(USER_ID, "goal-1"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deactivateMarksGoalInactive() {
        Goal goal = new Goal();
        goal.setUserId(USER_ID);
        goal.setTitle("Study");
        goal.setFrequency(Frequency.DAILY);
        when(goalRepository.findByIdAndUserId("goal-1", USER_ID)).thenReturn(Optional.of(goal));

        goalService.deactivate(USER_ID, "goal-1");

        assertThat(goal.isActive()).isFalse();
    }
}
