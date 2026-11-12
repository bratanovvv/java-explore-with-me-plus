package ru.practicum.comment;

import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.comment.dto.request.CommentStatusUpdateRequest;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.compilation.BaseTestClass;

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
@DisplayName("Интеграционные тесты Комментариев Public")
public class PublicCommentTest extends BaseTestClass {

    private Long userId;
    private Long categoryId;
    private Long eventId;

    @BeforeEach
    public void beforeEach() throws Exception {
        userId = getIdFromObject(createUser());
        categoryId = getIdFromObject(createCategory());
        eventId = getIdFromObject(createEvent(userId, categoryId));
        publishEvent(eventId);
    }

    @Test
    public void testGetComments_shouldReturnOnlyPublishedWithoutModerationFields() throws Exception {
        long publishedId = getIdFromObject(createComment("Опубликованный"));
        long rejectedId = getIdFromObject(createComment("Отклонённый"));
        createComment("На модерации");

        moderate(publishedId, CommentStatus.PUBLISHED, null);
        moderate(rejectedId, CommentStatus.REJECTED, "Содержит спам");

        mockMvc.perform(get("/comments/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(publishedId))
                .andExpect(jsonPath("$.content[0].text").value("Опубликованный"))
                .andExpect(jsonPath("$.content[0].eventId").value(eventId))
                .andExpect(jsonPath("$.content[0].authorId").value(userId))
                .andExpect(jsonPath("$.content[0].created").isNotEmpty())
                .andExpect(jsonPath("$.content[0].status").doesNotExist())
                .andExpect(jsonPath("$.content[0].rejectionReason").doesNotExist());
    }

    @Test
    public void testGetComments_shouldSortNewestByDefaultAndOldestOnDemand() throws Exception {
        long firstId = getIdFromObject(createComment("Первый"));
        long secondId = getIdFromObject(createComment("Второй"));
        moderate(firstId, CommentStatus.PUBLISHED, null);
        moderate(secondId, CommentStatus.PUBLISHED, null);

        mockMvc.perform(get("/comments/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(secondId))
                .andExpect(jsonPath("$.content[1].id").value(firstId));

        mockMvc.perform(get("/comments/" + eventId).param("sort", "OLDEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(firstId))
                .andExpect(jsonPath("$.content[1].id").value(secondId));
    }

    @Test
    public void testGetComments_shouldApplyPagination() throws Exception {
        long firstId = getIdFromObject(createComment("Первый"));
        long secondId = getIdFromObject(createComment("Второй"));
        long thirdId = getIdFromObject(createComment("Третий"));
        moderate(firstId, CommentStatus.PUBLISHED, null);
        moderate(secondId, CommentStatus.PUBLISHED, null);
        moderate(thirdId, CommentStatus.PUBLISHED, null);

        mockMvc.perform(get("/comments/" + eventId).param("from", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(thirdId))
                .andExpect(jsonPath("$.content[1].id").value(secondId));

        mockMvc.perform(get("/comments/" + eventId).param("from", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(firstId));
    }

    @Test
    public void testGetCommentsForUnpublishedEvent_shouldReturnNotFound() throws Exception {
        Long draftEventId = getIdFromObject(createEvent(userId, categoryId));

        mockMvc.perform(get("/comments/" + draftEventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    public void testGetCommentsForUnknownEvent_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/comments/" + (eventId + 999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    public void testGetComments_invalidPagination_shouldReturnBadRequestInEnglish() throws Exception {
        mockMvc.perform(get("/comments/" + eventId).param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("getComments.size: must be greater than 0"));
    }

    private ResultActions createComment(String text) throws Exception {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText(text);
        return mockMvc.perform(post("/users/" + userId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", eventId.toString())
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    private void publishEvent(Long eventId) throws Exception {
        mockMvc.perform(patch("/admin/events/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stateAction\" : \"PUBLISH_EVENT\"}"))
                .andExpect(status().isOk());
    }

    private void moderate(Long commentId, CommentStatus status, String rejectionReason) throws Exception {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(status);
        request.setRejectionReason(rejectionReason);

        mockMvc.perform(patch("/admin/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
