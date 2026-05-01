package ru.practicum.ewm.extra.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.extra.compilation.dto.CompilationDto;
import ru.practicum.ewm.extra.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.extra.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.extra.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto dto) {
        log.info("ADMIN: Create compilation: {}", dto.title());
        return compilationService.addCompilation(dto);
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto updateCompilation(
            @PathVariable Long compilationId,
            @RequestBody @Valid UpdateCompilationRequest dto) {
        log.info("ADMIN: Update compilation {}", compilationId);
        return compilationService.updateCompilation(compilationId, dto);
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compilationId) {
        log.info("ADMIN: Delete compilation {}", compilationId);
        compilationService.deleteCompilation(compilationId);
    }
}
