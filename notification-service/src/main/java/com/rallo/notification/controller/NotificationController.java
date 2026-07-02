package com.rallo.notification.controller;

import com.rallo.notification.dto.NotificationResponse;
import com.rallo.notification.exception.ErrorResponse;
import com.rallo.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications generated from check-in events")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List all notifications for the authenticated user, newest first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns list of notifications (empty list if none)"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<NotificationResponse>> list(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(notificationService.listForUser(userId));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count unread notifications for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number of unread notifications"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Long> unreadCount(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(notificationService.unreadCount(userId));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found or does not belong to this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<NotificationResponse> markRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.markRead(userId, notificationId));
    }
}
