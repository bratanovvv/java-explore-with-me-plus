package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.utils.error.exception.NotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@Import(CategoryMapper.class)
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void create_shouldReturn201() throws Exception {
        CategoryDto request = new CategoryDto(null, "Концерты");
        when(categoryService.create(any(Category.class))).thenReturn(new Category(1L, "Концерты"));

        postCategory(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    void create_blankName_shouldReturn400() throws Exception {
        postCategory(new CategoryDto(null, "   "))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void create_emptyName_shouldReturn400() throws Exception {
        postCategory(new CategoryDto(null, ""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void create_nullName_shouldReturn400() throws Exception {
        postCategory(new CategoryDto(null, null))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void create_tooLongName_shouldReturn400() throws Exception {
        postCategory(new CategoryDto(null, "a".repeat(51)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void create_duplicateName_shouldReturn409() throws Exception {
        when(categoryService.create(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate name"));

        postCategory(new CategoryDto(null, "Концерты"))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(categoryService.update(eq(1L), any(Category.class)))
                .thenReturn(new Category(1L, "Новое"));

        patchCategory(1L, new CategoryDto(null, "Новое"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Новое"));
    }

    @Test
    void update_blankName_shouldReturn400() throws Exception {
        patchCategory(1L, new CategoryDto(null, "   "))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void update_duplicateName_shouldReturn409() throws Exception {
        when(categoryService.update(eq(1L), any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate name"));

        patchCategory(1L, new CategoryDto(null, "Занято"))
                .andExpect(status().isConflict());
    }

    @Test
    void update_missing_shouldReturn404() throws Exception {
        when(categoryService.update(eq(99L), any(Category.class)))
                .thenThrow(new NotFoundException("Category with id=99 was not found"));

        patchCategory(99L, new CategoryDto(null, "Новое"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isNoContent());
        verify(categoryService).delete(1L);
    }

    @Test
    void delete_missing_shouldReturn404() throws Exception {
        doThrow(new NotFoundException("Category with id=99 was not found")).when(categoryService).delete(99L);

        mockMvc.perform(delete("/admin/categories/{catId}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_withEvents_shouldReturn409() throws Exception {
        doThrow(new DataIntegrityViolationException("category is not empty")).when(categoryService).delete(1L);

        mockMvc.perform(delete("/admin/categories/{catId}", 1L))
                .andExpect(status().isConflict());
    }

    private ResultActions postCategory(CategoryDto request) throws Exception {
        return mockMvc.perform(post("/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions patchCategory(Long catId, CategoryDto request) throws Exception {
        return mockMvc.perform(patch("/admin/categories/{catId}", catId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
