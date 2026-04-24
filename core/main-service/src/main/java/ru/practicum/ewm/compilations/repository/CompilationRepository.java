package ru.practicum.ewm.compilations.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.compilations.model.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @EntityGraph("compilation-with-events")
    @Query("""
            SELECT DISTINCT c FROM Compilation c
            WHERE :pinned IS NULL OR c.pinned = :pinned
            """)
    Page<Compilation> findAllByPinned(
            @Param("pinned") @Nullable Boolean pinned,
            Pageable pageable);
}
