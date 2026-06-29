package com.rallo.checkin.controller;

import com.rallo.checkin.dto.CreateGoalRequest;
import com.rallo.checkin.dto.GoalResponse;
import com.rallo.checkin.service.GoalService;
import com.rallo.checkin.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Create and manage recurring goals")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @Operation(summary = "Create a new goal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Goal created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GoalResponse> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List all active goals for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns list of active goals (empty list if none)"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<GoalResponse>> list(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(goalService.listActive(userId));
    }

    @GetMapping("/{goalId}")
    @Operation(summary = "Get a single goal by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goal found"),
            @ApiResponse(responseCode = "404", description = "Goal not found or does not belong to this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GoalResponse> get(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String goalId) {
        return ResponseEntity.ok(goalService.get(userId, goalId));
    }

    @DeleteMapping("/{goalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a goal")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Goal deactivated"),
            @ApiResponse(responseCode = "404", description = "Goal not found or does not belong to this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public void deactivate(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String goalId) {
        goalService.deactivate(userId, goalId);
    }
}
