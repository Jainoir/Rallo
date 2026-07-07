package com.rallo.checkin.controller;

import com.rallo.checkin.dto.LeaderboardEntry;
import com.rallo.checkin.service.SocialStreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/checkins/streaks")
@RequiredArgsConstructor
@Tag(name = "Leaderboards", description = "Streak standings across friends and groups")
public class LeaderboardController {

    private final SocialStreakService socialStreakService;

    @GetMapping("/friends")
    @Operation(summary = "Streak leaderboard across the authenticated user and their friends")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entries sorted by best current streak, self included"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<LeaderboardEntry>> friends(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "You") String username,
            @RequestHeader(value = "X-Timezone", required = false) String timezone) {
        return ResponseEntity.ok(
                socialStreakService.friendsLeaderboard(userId, username, zoneOf(timezone)));
    }

    @GetMapping("/groups/{groupId}")
    @Operation(summary = "Streak leaderboard for a group the authenticated user belongs to")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group members sorted by best current streak"),
            @ApiResponse(responseCode = "404", description = "Group not found or user is not a member"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<LeaderboardEntry>> group(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-Timezone", required = false) String timezone,
            @PathVariable String groupId) {
        return ResponseEntity.ok(
                socialStreakService.groupLeaderboard(groupId, userId, zoneOf(timezone)));
    }

    private static ZoneId zoneOf(String timezone) {
        if (timezone == null || timezone.isBlank()) return ZoneOffset.UTC;
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            return ZoneOffset.UTC;
        }
    }
}
