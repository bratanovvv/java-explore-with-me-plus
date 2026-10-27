package ru.practicum.category.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @Test
    void toDto_shouldMapAllFields() {
        Category category = new Category(1L, "Концерты");

        CategoryDto dto = categoryMapper.toDto(category);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Концерты");
    }

    @Test
    void toEntity_shouldMapAllFields() {
        CategoryDto dto = new CategoryDto(1L, "Концерты");

        Category category = categoryMapper.toEntity(dto);

        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("Концерты");
    }

    @Test
    void toDtoList_shouldMapAllCategories() {
        List<Category> categories = List.of(
                new Category(1L, "Концерты"),
                new Category(2L, "Выставки"));

        List<CategoryDto> dtos = categoryMapper.toDtoList(categories);

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(CategoryDto::getName).containsExactly("Концерты", "Выставки");
    }
}
