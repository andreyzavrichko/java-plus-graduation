package ru.practicum.ewm.sharing.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AtLeastHoursFromNowValidator
        implements ConstraintValidator<AtLeastHoursFromNow, LocalDateTime> {

    private int hours;
    private ChronoUnit unit;

    @Override
    public void initialize(AtLeastHoursFromNow constraintAnnotation) {
        this.hours = constraintAnnotation.hours();
        this.unit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minAllowedTime = now.minus(hours, unit);

        return value.isAfter(minAllowedTime);
    }
}
