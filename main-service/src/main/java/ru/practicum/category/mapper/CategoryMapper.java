package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.utils.interfaces.Mapper;

@Component
public class CategoryMapper implements Mapper<Category, CategoryDto> {

    @Override
    public CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    @Override
    public Category toEntity(CategoryDto dto) {
        return new Category(dto.getId(), dto.getName());
    }
}
