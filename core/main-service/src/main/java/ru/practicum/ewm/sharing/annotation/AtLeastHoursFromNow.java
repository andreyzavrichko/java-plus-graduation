package ru.practicum.ewm.sharing.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {AtLeastHoursFromNowValidator.class})
public @interface AtLeastHoursFromNow {

    String message() default "Event must be at least {hours} hours from now";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int hours() default 0;

    ChronoUnit unit() default ChronoUnit.HOURS;
}
