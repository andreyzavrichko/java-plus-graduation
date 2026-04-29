package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.api.sharing.PageableFactory;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.CreateCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.sharing.BaseService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends BaseService implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(CreateCategoryDto dto) {
        if (categoryRepository.existsByName(dto.name()))
            throw new ConflictException("Category with name " + dto.name() + " already exists");
        return categoryMapper.toDto(categoryRepository.save(categoryMapper.toEntity(dto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        if (categoryRepository.existsByNameAndIdNot(dto.name(), id))
            throw new ConflictException("Category with name " + dto.name() + " already exists");
        Category category = findCategoryOrThrow(id);
        category.setName(dto.name());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        return categoryMapper.toDto(findCategoryOrThrow(id));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        findCategoryOrThrow(id);
        if (eventRepository.existsByCategoryId(id))
            throw new ConflictException("The category is not empty");
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageableFactory.offset(from, size);
        return categoryRepository.findAll(pageable).getContent()
                .stream().map(categoryMapper::toDto).collect(Collectors.toList());
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> throwNotFound(id));
    }
}
