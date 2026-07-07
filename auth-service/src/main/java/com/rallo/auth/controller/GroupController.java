package com.rallo.auth.controller;

import com.rallo.auth.dto.AddGroupMemberRequest;
import com.rallo.auth.dto.CreateGroupRequest;
import com.rallo.auth.dto.GroupMemberResponse;
import com.rallo.auth.dto.GroupResponse;
import com.rallo.auth.exception.ErrorResponse;
import com.rallo.auth.service.GroupService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Accountability groups — shared leaderboards among members")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Create a group (creator becomes owner and first member)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GroupResponse> create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(userId, request.name()));
    }

    @GetMapping
    @Operation(summary = "List groups the authenticated user belongs to")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Groups (empty list if none)"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<GroupResponse>> myGroups(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(groupService.myGroups(userId));
    }

    @PostMapping("/{groupId}/members")
    @Operation(summary = "Add a member by username (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member added"),
            @ApiResponse(responseCode = "400", description = "Already a member",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Group/user not found, or caller is not the owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<GroupMemberResponse> addMember(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.addMember(userId, groupId, request.username()));
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "List members of a group (members only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Members with usernames"),
            @ApiResponse(responseCode = "404", description = "Group not found or caller is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<GroupMemberResponse>> members(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String groupId) {
        return ResponseEntity.ok(groupService.members(userId, groupId));
    }
}
