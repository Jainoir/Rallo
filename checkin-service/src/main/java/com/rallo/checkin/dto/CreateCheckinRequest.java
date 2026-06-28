package com.rallo.checkin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCheckinRequest(
        @NotNull LocalDate checkinDate,
        @Size(max = 500) String notes
) {}
