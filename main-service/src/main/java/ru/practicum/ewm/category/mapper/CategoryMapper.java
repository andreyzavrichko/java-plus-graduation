package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CategoryInfo;
import ru.practicum.ewm.category.dto.CreateCategoryDto;
import ru.practicum.ewm.category.model.Category;

@Mapper
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toEntity(CreateCategoryDto createCategoryDto);

    CategoryDto toDto(Category category);

    CategoryDto toDto(CategoryInfo projection);
}
