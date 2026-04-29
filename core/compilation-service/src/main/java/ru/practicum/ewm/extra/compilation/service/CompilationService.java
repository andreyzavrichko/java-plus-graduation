package ru.practicum.ewm.extra.compilation.service;

import ru.practicum.ewm.extra.compilation.dto.CompilationDto;
import ru.practicum.ewm.extra.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.extra.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto dto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto);

    void deleteCompilation(Long compId);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);
}
