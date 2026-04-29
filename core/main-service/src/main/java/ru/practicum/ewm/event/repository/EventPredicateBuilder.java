package ru.practicum.ewm.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class EventPredicateBuilder {
    private static final QEvent event = QEvent.event;
    private final BooleanBuilder builder = new BooleanBuilder();

    public EventPredicateBuilder withInitiators(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) builder.and(event.initiatorId.in(ids));
        return this;
    }

    public EventPredicateBuilder withStates(List<State> states) {
        if (states != null && !states.isEmpty()) builder.and(event.state.in(states));
        return this;
    }

    public EventPredicateBuilder withCategories(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) builder.and(event.category.id.in(ids));
        return this;
    }

    public EventPredicateBuilder withDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        builder.and(event.eventDate.goe(Objects.requireNonNullElseGet(rangeStart, LocalDateTime::now)));
        if (rangeEnd != null) builder.and(event.eventDate.loe(rangeEnd));
        return this;
    }

    public EventPredicateBuilder withPaid(Boolean paid) {
        if (paid != null) builder.and(event.paid.eq(paid));
        return this;
    }

    public EventPredicateBuilder withTextSearch(String text) {
        if (text != null && !text.isEmpty()) {
            String t = text.trim().toLowerCase();
            builder.and(event.annotation.lower().contains(t).or(event.description.lower().contains(t)));
        }
        return this;
    }

    public EventPredicateBuilder forPublicSearch() {
        builder.and(event.state.eq(State.PUBLISHED));
        return this;
    }

    public Predicate build() {
        Predicate p = builder.getValue();
        return p != null ? p : Expressions.TRUE.isTrue();
    }
}
