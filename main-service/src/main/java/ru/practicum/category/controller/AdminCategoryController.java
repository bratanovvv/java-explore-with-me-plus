package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public AdminCategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody CategoryDto categoryDto) {
        Category category = categoryService.create(categoryMapper.toEntity(categoryDto));
        return categoryMapper.toDto(category);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable Long catId, @Valid @RequestBody CategoryDto categoryDto) {
        Category category = categoryService.update(catId, categoryMapper.toEntity(categoryDto));
        return categoryMapper.toDto(category);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long catId) {
        categoryService.delete(catId);
    }
}
