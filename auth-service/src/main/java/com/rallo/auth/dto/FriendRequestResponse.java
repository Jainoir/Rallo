package com.rallo.auth.dto;

import java.time.Instant;

public record FriendRequestResponse(
        String id,
        String requesterId,
        String requesterUsername,
        Instant createdAt
) {}
