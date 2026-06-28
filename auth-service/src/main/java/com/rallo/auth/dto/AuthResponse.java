package com.rallo.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String username
) {}
