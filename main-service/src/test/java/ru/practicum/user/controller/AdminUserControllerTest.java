package ru.practicum.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(UserMapper.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturn201() throws Exception {
        UserDto request = new UserDto(null, "Иван Петров", "ivan@example.com");
        User savedUser = new User(1L, "Иван Петров", "ivan@example.com");
        when(userService.create(any(User.class))).thenReturn(savedUser);

        postUser(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Иван Петров"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));
    }

    @Test
    void create_withIdInBody_shouldIgnoreId() throws Exception {
        UserDto request = new UserDto(99L, "Иван Петров", "ivan@example.com");
        when(userService.create(any(User.class)))
                .thenReturn(new User(1L, "Иван Петров", "ivan@example.com"));

        postUser(request).andExpect(status().isCreated());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isNull();
    }

    @Test
    void create_withBlankName_shouldReturn400() throws Exception {
        UserDto request = new UserDto(null, "   ", "ivan@example.com");

        postUser(request).andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void create_withTooShortName_shouldReturn400() throws Exception {
        UserDto request = new UserDto(null, "A", "ivan@example.com");

        postUser(request).andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void create_withTooShortEmail_shouldReturn400() throws Exception {
        UserDto request = new UserDto(null, "Иван Петров", "a@a.r");

        postUser(request).andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void create_withBlankEmail_shouldReturn400() throws Exception {
        UserDto request = new UserDto(null, "Иван Петров", "       ");

        postUser(request).andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void create_withMalformedEmail_shouldReturn400() throws Exception {
        UserDto request = new UserDto(null, "Иван Петров", "not-an-email");

        postUser(request).andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void create_withExistingEmail_shouldReturn409() throws Exception {
        UserDto request = new UserDto(null, "Иван Петров", "ivan@example.com");
        when(userService.create(any(User.class)))
                .thenThrow(new ConflictException("User with email ivan@example.com already exists"));

        postUser(request).andExpect(status().isConflict());
    }

    @Test
    void getUsers_withIds_shouldReturnFilteredList() throws Exception {
        List<User> response = List.of(
                new User(1L, "Иван Петров", "ivan@example.com"),
                new User(2L, "Пётр Иванов", "petr@example.com"));
        when(userService.getUsers(eq(List.of(1L, 2L)), any(PageParams.class))).thenReturn(response);

        mockMvc.perform(get("/admin/users").param("ids", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].email").value("petr@example.com"));

        verify(userService).getUsers(List.of(1L, 2L), new PageParams(0, 10));
    }

    @Test
    void getUsers_withoutParams_shouldUseDefaultPagination() throws Exception {
        when(userService.getUsers(isNull(), any(PageParams.class))).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getUsers(null, new PageParams(0, 10));
    }

    @Test
    void getUsers_withNegativeFrom_shouldReturn400() throws Exception {
        mockMvc.perform(get("/admin/users").param("from", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void getUsers_withZeroSize_shouldReturn400() throws Exception {
        mockMvc.perform(get("/admin/users").param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void delete_withMissingUser_shouldReturn404() throws Exception {
        doThrow(new NotFoundException("User with id=99 was not found")).when(userService).delete(99L);

        mockMvc.perform(delete("/admin/users/{userId}", 99L))
                .andExpect(status().isNotFound());
    }

    private ResultActions postUser(UserDto request) throws Exception {
        return mockMvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
