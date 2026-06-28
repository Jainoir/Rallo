package com.rallo.checkin.controller;

import com.rallo.checkin.dto.CreateCheckinRequest;
import com.rallo.checkin.events.CheckinEventPublisher;
import com.rallo.checkin.events.CheckinRecordedEvent;
import com.rallo.checkin.model.Checkin;
import com.rallo.checkin.model.Goal;
import com.rallo.checkin.repository.CheckinRepository;
import com.rallo.checkin.repository.GoalRepository;
import com.rallo.checkin.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
@Tag(name = "Check-ins", description = "Record check-ins and view streak data")
public class CheckinController {

    private final GoalRepository goalRepository;
    private final CheckinRepository checkinRepository;
    private final StreakService streakService;
    private final CheckinEventPublisher eventPublisher;

    @PostMapping("/goals/{goalId}")
    @Operation(summary = "Check in for a goal")
    public ResponseEntity<Void> checkin(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String goalId,
            @Valid @RequestBody CreateCheckinRequest request) {

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found"));

        if (checkinRepository.existsByGoalIdAndCheckinDate(goalId, request.checkinDate())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Checkin checkin = new Checkin();
        checkin.setGoal(goal);
        checkin.setUserId(userId);
        checkin.setCheckinDate(request.checkinDate());
        checkin.setNotes(request.notes());
        checkinRepository.save(checkin);

        int streak = streakService.currentStreak(goalId, goal.getFrequency());
        eventPublisher.publishCheckinRecorded(
                new CheckinRecordedEvent(userId, goalId, goal.getTitle(), request.checkinDate(), streak));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/goals/{goalId}/streak")
    @Operation(summary = "Get current streak for a goal")
    public ResponseEntity<Integer> streak(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String goalId) {

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new NoSuchElementException("Goal not found"));

        return ResponseEntity.ok(streakService.currentStreak(goalId, goal.getFrequency()));
    }
}
