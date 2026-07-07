package com.rallo.auth.controller;

import com.rallo.auth.dto.FriendRequestResponse;
import com.rallo.auth.dto.FriendResponse;
import com.rallo.auth.dto.SendFriendRequestRequest;
import com.rallo.auth.exception.ErrorResponse;
import com.rallo.auth.service.FriendService;
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
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "Friend requests and the accountability social graph")
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    @Operation(summary = "List accepted friends of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Friends with their usernames (empty list if none)"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<FriendResponse>> friends(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(friendService.listFriends(userId));
    }

    @PostMapping("/requests")
    @Operation(summary = "Send a friend request by username")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Request created"),
            @ApiResponse(responseCode = "400", description = "Self-request or request already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No user with that username",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<FriendRequestResponse> sendRequest(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SendFriendRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendService.sendRequest(userId, request.username()));
    }

    @GetMapping("/requests")
    @Operation(summary = "List incoming pending friend requests")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending requests addressed to this user"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<FriendRequestResponse>> incoming(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(friendService.incomingRequests(userId));
    }

    @PostMapping("/requests/{requestId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Accept a pending friend request addressed to you")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Request accepted"),
            @ApiResponse(responseCode = "404", description = "Request not found or not addressed to this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public void accept(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String requestId) {
        friendService.accept(userId, requestId);
    }
}
