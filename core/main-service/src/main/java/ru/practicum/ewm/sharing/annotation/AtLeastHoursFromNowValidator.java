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
    public void initialize(AtLeastHoursFromNow a) {
        this.hours = a.hours();
        this.unit = a.unit();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        return value.isAfter(LocalDateTime.now().plus(hours, unit));
    }
}