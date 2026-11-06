package ru.practicum.category.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCategoryController.class)
@Import(CategoryMapper.class)
class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    void getCategories_shouldReturnList() throws Exception {
        when(categoryService.getCategories(any(PageParams.class)))
                .thenReturn(List.of(new Category(1L, "Концерты"), new Category(2L, "Выставки")));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Концерты"));

        verify(categoryService).getCategories(new PageParams(0, 10));
    }

    @Test
    void getCategories_withPagination_shouldPassParams() throws Exception {
        when(categoryService.getCategories(any(PageParams.class))).thenReturn(List.of());

        mockMvc.perform(get("/categories").param("from", "5").param("size", "20"))
                .andExpect(status().isOk());

        verify(categoryService).getCategories(new PageParams(5, 20));
    }

    @Test
    void getCategories_negativeFrom_shouldReturn400() throws Exception {
        mockMvc.perform(get("/categories").param("from", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoryService);
    }

    @Test
    void getCategory_shouldReturnCategory() throws Exception {
        when(categoryService.getCategory(1L)).thenReturn(new Category(1L, "Концерты"));

        mockMvc.perform(get("/categories/{catId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Концерты"));
    }

    @Test
    void getCategory_missing_shouldReturn404() throws Exception {
        when(categoryService.getCategory(99L))
                .thenThrow(new NotFoundException("Category with id=99 was not found"));

        mockMvc.perform(get("/categories/{catId}", 99L))
                .andExpect(status().isNotFound());
    }
}
