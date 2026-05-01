package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.*;

public record NewUserRequest(
        @NotBlank @Size(min = 2, max = 250) String name,
        @NotBlank @Email @Size(min = 6, max = 254) String email
) {}
