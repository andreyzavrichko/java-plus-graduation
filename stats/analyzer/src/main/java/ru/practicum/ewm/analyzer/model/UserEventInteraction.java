package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "user_event_interaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventInteraction {

    @EmbeddedId
    private UserEventId id;

    @Column(nullable = false)
    private Double weight;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserEventId implements Serializable {
        @Column(name = "user_id")
        private Long userId;
        @Column(name = "event_id")
        private Long eventId;
    }
}