package ru.practicum.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.event.dto.common.LocationDto;
import ru.practicum.event.dto.request.EventRequestDto;
import ru.practicum.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class CompilationBaseTestClass {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final EventRequestDto event = new EventRequestDto();

    protected long getIdFromObject(ResultActions resultActions) throws Exception {
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(responseBody, "$.id")).longValue();
    }

    protected ResultActions createUser() throws Exception {
        return mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UserDto.builder()
                                .name("Иван Петров")
                                .email("ivan.petrov@practicummail.ru").build())))
                .andExpect(status().isCreated());
    }

    protected ResultActions createEvent(long userId, long catId) throws Exception {
        return mockMvc.perform(post("/users/" + userId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fillEvent(catId))))
                .andExpect(status().isCreated());
    }

    protected ResultActions createCompilation(Set<Long> listEvents, Boolean pinned, String title) throws Exception {
        return mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CompilationDto.builder()
                                .eventsId(listEvents)
                                .pinned(pinned)
                                .title(title)
                                .build())))
                .andExpect(status().isCreated());
    }

    protected ResultActions createCategory() throws Exception {
        return mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(CategoryDto.builder()
                                .name("New Category")
                                .build())))
                .andExpect(status().isCreated());
    }

    protected EventRequestDto fillEvent(long catId) {
        event.setAnnotation("Это достаточно длинная аннотация для события");
        event.setCategory(catId);
        event.setDescription("Это достаточно длинное описание события");
        event.setEventDate(LocalDateTime.now().plusDays(2));
        event.setLocation(new LocationDto(55.75, 37.61));
        event.setTitle("Название события");
        return event;
    }
}