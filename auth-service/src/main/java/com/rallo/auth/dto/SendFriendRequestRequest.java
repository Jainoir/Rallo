package com.rallo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SendFriendRequestRequest(
        @NotBlank String username
) {}
