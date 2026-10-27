package ru.practicum.category.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getCategories_shouldReturnSortedPage() {
        Category category = new Category(1L, "Концерты");
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(category)));

        List<Category> actual = categoryService.getCategories(new PageParams(0, 10));

        assertThat(actual).containsExactly(category);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryRepository).findAll(captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void getCategory_shouldReturnCategory() {
        Category category = new Category(1L, "Концерты");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category actual = categoryService.getCategory(1L);

        assertThat(actual).isEqualTo(category);
    }

    @Test
    void getCategory_missing_shouldThrowNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategory(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveAndReturnCategory() {
        Category category = new Category(null, "Концерты");
        when(categoryRepository.save(category)).thenReturn(new Category(1L, "Концерты"));

        Category actual = categoryService.create(category);

        assertThat(actual.getId()).isEqualTo(1L);
        verify(categoryRepository).save(category);
    }

    @Test
    void create_duplicateName_shouldPropagateDataIntegrityViolation() {
        Category category = new Category(null, "Концерты");
        when(categoryRepository.save(category))
                .thenThrow(new DataIntegrityViolationException("duplicate name"));

        assertThatThrownBy(() -> categoryService.create(category))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void update_shouldRenameCategory() {
        Category existing = new Category(1L, "Старое");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        Category actual = categoryService.update(1L, new Category(1L, "Новое"));

        assertThat(actual.getName()).isEqualTo("Новое");
    }

    @Test
    void update_sameName_shouldNotThrow() {
        Category existing = new Category(1L, "Концерты");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        Category actual = categoryService.update(1L, new Category(1L, "Концерты"));

        assertThat(actual.getName()).isEqualTo("Концерты");
    }

    @Test
    void update_missing_shouldThrowNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, new Category(99L, "Новое")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_shouldDeleteCategory() {
        Category category = new Category(1L, "Концерты");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_missing_shouldThrowNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
