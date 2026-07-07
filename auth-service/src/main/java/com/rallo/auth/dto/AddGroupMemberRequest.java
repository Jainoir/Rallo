package com.rallo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AddGroupMemberRequest(
        @NotBlank String username
) {}
