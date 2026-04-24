package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCompilationRequest(
        List<Long> events,

        Boolean pinned,

        @Size(min = 1, max = 50)
        String title
) {
}
