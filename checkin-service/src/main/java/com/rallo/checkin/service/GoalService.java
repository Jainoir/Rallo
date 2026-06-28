package com.rallo.checkin.service;

import com.rallo.checkin.dto.CreateGoalRequest;
import com.rallo.checkin.dto.GoalResponse;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;

    @Transactional
    public GoalResponse create(String userId, CreateGoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setFrequency(request.frequency());
        goal.setTargetDaysPerWeek(request.targetDaysPerWeek());
        return GoalResponse.from(goalRepository.save(goal));
    }

    public List<GoalResponse> listActive(String userId) {
        return goalRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(GoalResponse::from)
                .toList();
    }

    public GoalResponse get(String userId, String goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .map(GoalResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Goal not found: " + goalId));
    }

    @Transactional
    public void deactivate(String userId, String goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found: " + goalId));
        goal.setActive(false);
    }
}
