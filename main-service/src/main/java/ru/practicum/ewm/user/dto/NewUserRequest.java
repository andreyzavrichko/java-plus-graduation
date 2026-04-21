package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewUserRequest(

        @NotBlank(message = "Имя не должно быть пустым")
        @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250 символов")
        String name,

        @Email(message = "Некорректный email")
        @NotBlank(message = "Email не может быть пустым")
        @Size(min = 6, max = 254, message = "Email слишком длинный")
        String email
) {
}
