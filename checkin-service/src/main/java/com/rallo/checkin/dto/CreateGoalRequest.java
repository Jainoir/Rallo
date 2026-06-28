package com.rallo.checkin.dto;

import com.rallo.checkin.model.Frequency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGoalRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 500) String description,
        @NotNull Frequency frequency,
        @Min(1) @Max(7) Integer targetDaysPerWeek
) {}
