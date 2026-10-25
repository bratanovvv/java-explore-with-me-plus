package ru.practicum.category.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories(PageParams pageParams) {
        Pageable pageable = pageParams.toPageable(Sort.by(Sort.Direction.ASC, "id"));
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.getContent();
    }

    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    @Transactional
    public Category create(Category category) {
        Category savedCategory = categoryRepository.save(category);
        log.debug("Создана категория id={}", savedCategory.getId());
        return savedCategory;
    }

    @Transactional
    public Category update(Long categoryId, Category category) {
        Category existingCategory = getCategory(categoryId);
        existingCategory.setName(category.getName());
        log.debug("Обновлена категория id={}", categoryId);
        return existingCategory;
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = getCategory(categoryId);
        categoryRepository.delete(category);
        log.debug("Удалена категория id={}", categoryId);
    }
}
