package ru.practicum.ewm;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreateHitDto(
        @NotBlank(message = "App не может быть пустым")
        @Size(max = 255, message = "App слишком длинный")
        String app,

        @NotBlank(message = "Uri не может быть пустым")
        @Size(max = 512, message = "Uri слишком длинный")
        String uri,

        @NotBlank(message = "IP не может быть пустым")
        @Pattern(
                regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$",
                message = "Некорректный формат IP-адреса"
        )
        String ip,

        @NotNull(message = "Timestamp не может быть null")
        @PastOrPresent(message = "Timestamp не может быть в будущем")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
}