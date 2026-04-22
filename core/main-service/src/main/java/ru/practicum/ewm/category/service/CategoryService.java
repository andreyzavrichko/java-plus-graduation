package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CreateCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(CreateCategoryDto createCategoryDto);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);

    CategoryDto getCategoryById(Long id);

    void deleteCategory(Long id);

    List<CategoryDto> getCategories(Integer from, Integer size);
}
