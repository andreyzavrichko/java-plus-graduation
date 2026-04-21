package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CreateCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.sharing.constants.ApiPaths;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Admin.CATEGORIES)
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CreateCategoryDto request) {
        log.info("ADMIN: Create category {}", request);
        CategoryDto result = service.addCategory(request);
        log.info("ADMIN: Created category {}", result);
        return result;
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable Long catId, @RequestBody @Valid CategoryDto categoryDto) {
        log.info("ADMIN: Update category {}", categoryDto);
        CategoryDto result = service.updateCategory(catId, categoryDto);
        log.info("ADMIN: Updated category {}", result);
        return result;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long catId) {
        log.info("ADMIN: Delete category {}", catId);
        service.deleteCategory(catId);
        log.info("ADMIN: Deleted category {}", catId);
    }
}
