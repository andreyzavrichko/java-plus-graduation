package ru.practicum.ewm.repository;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.ViewStatsDto;
import ru.practicum.ewm.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcStatsRepository {
    private final NamedParameterJdbcOperations jdbc;

    public Hit save(Hit hit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("app", hit.getApp().getValue())
                .addValue("uri", hit.getUri())
                .addValue("ip", hit.getIp())
                .addValue("timestamp", hit.getTimestamp());

        String sql = """
                INSERT INTO hits (app, uri, ip, timestamp)
                VALUES (:app, :uri, :ip::inet, :timestamp)
                """;

        jdbc.update(sql, params);
        return hit;
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, @Nullable List<String> uris,
                                       boolean unique) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);

        String counting = unique ? "COUNT(DISTINCT ip)" : "COUNT(*)";
        String checkedUris;

        if (uris == null || uris.isEmpty()) {
            checkedUris = "";
        } else {
            checkedUris = " AND uri IN (:uris)";
            params.addValue("uris", uris);
        }

        String sql = """
                SELECT app, uri, %s AS hits
                FROM hits
                WHERE timestamp BETWEEN :start AND :end%s
                GROUP BY app, uri
                ORDER BY hits DESC
                """.formatted(counting, checkedUris);

        return jdbc.query(sql, params, (rs, rowNum) ->
                new ViewStatsDto(
                        rs.getString("app"),
                        rs.getString("uri"),
                        rs.getLong("hits")
                ));
    }
}