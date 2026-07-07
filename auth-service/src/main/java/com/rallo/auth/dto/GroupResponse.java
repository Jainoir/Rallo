package com.rallo.auth.dto;

import com.rallo.auth.model.Group;

import java.time.Instant;

public record GroupResponse(
        String id,
        String name,
        String ownerId,
        Instant createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(group.getId(), group.getName(), group.getOwnerId(), group.getCreatedAt());
    }
}
