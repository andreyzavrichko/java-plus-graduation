package ru.practicum.ewm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "event_similarity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSimilarity {

    @EmbeddedId
    private EventSimilarityId id;

    @Column(nullable = false)
    private Double score;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EventSimilarityId implements Serializable {
        @Column(name = "event_a")
        private Long eventA;
        @Column(name = "event_b")
        private Long eventB;
    }
}