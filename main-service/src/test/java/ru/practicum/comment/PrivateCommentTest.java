package ru.practicum.comment;


import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.request.CommentRequestDto;
import ru.practicum.compilation.BaseTestClass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Интеграционные тесты Комментариев Private")
public class PrivateCommentTest extends BaseTestClass {
    private Long userId;
    private Long catId;
    private Long eventId;

    @BeforeEach
    public void beforeEach() throws Exception {
        userId = getIdFromObject(createUser());
        catId = getIdFromObject(createCategory());
        eventId = getIdFromObject(createEvent(userId, catId));
    }

    @Test
    public void testCreateComment_shouldReturnOkAndNewCommentId() throws Exception {

        publishEvent(eventId);

        createComment("Первый!")
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("Первый!"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    private @NonNull ResultActions createComment(String text) throws Exception {
        CommentRequestDto comment = new CommentRequestDto();
        comment.setText(text);

        return mockMvc.perform(post("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", eventId.toString())
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated());
    }

    private void publishEvent(Long eventId) throws Exception {
        mockMvc.perform(patch("/admin/events/" + eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stateAction\" : \"PUBLISH_EVENT\"}")).andExpect(status().isOk());
    }

    @Test
    public void testCreateCommendWhenUserNotExist_ShouldReturnNotFound() throws Exception {
        CommentRequestDto comment = new CommentRequestDto();
        comment.setText("Первый!");
        //задаем не существуешего пользователся
        userId++;
        mockMvc.perform(post("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", eventId.toString())
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateCommentWhenNotExistEvent_ShouldReturnNotFound() throws Exception {
        CommentRequestDto comment = new CommentRequestDto();
        comment.setText("Первый!");
        //задаю несуществующие событие
        eventId++;
        mockMvc.perform(post("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", eventId.toString())
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateCommentWhenEventNotPublished_ShouldReturnNotFound() throws Exception {
        CommentRequestDto comment = new CommentRequestDto();
        comment.setText("Первый!");

        mockMvc.perform(post("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", eventId.toString())
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testGettingCommentsByUserWithDefaultParams_ShouldReturnListComments() throws Exception {
        publishEvent(eventId);
        createComment("Первый!");
        createComment("Второй!");

        mockMvc.perform(get("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].text").value("Первый!"));
    }

    @Test
    public void testGettingCommentByWrongUser_ShouldReturn404() throws Exception {
        publishEvent(eventId);
        createComment("Первый!");
        createComment("Второй!");
        //запрос от несуществующего пользователя
        userId++;
        mockMvc.perform(get("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteComment_ShouldReturn204() throws Exception {
        publishEvent(eventId);
        long id = getIdFromObject(createComment("Ghbdtn!"));
        createComment("Привет!");
        log.info("User = {} , Event1 = {} , Event2 = {}",userId,eventId,id);
        mockMvc.perform(delete("/users/" + userId + "/comments" + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Привет!"));
    }

    @Test
    public void testDeleteCommentWhenUserNotAuthor_ShouldReturn404() throws Exception {
        publishEvent(eventId);
        long id = getIdFromObject(createComment("Ghbdtn!"));
        createComment("Привет!");
        userId++;
        mockMvc.perform(delete("/users/" + userId + "/comments" + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        userId--;
        mockMvc.perform(get("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].text").value("Ghbdtn!"))
                .andExpect(jsonPath("$[0].text").value("Привет!"));
    }
}


