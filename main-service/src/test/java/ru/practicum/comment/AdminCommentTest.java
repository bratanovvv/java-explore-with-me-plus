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
@DisplayName("Интеграционные тесты Комментариев Admin")
public class AdminCommentTest extends BaseTestClass {

    private static final String URL = "/admin/comments";

    private Long userId;
    private Long eventId;

    @BeforeEach
    public void beforeEach() throws Exception {
        userId = getIdFromObject(createUser());
        Long categoryId = getIdFromObject(createCategory());
        eventId = getIdFromObject(createEvent(userId, categoryId));
        publishEvent(eventId);
    }

    @Test
    public void testGetComments_shouldFilterByStatusEventAndPagination() throws Exception {
        long pendingId = getIdFromObject(createComment("На модерации"));
        long publishedId = getIdFromObject(createComment("Опубликованный"));
        moderate(publishedId, CommentStatus.PUBLISHED, null);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").exists())
                .andExpect(jsonPath("$.content[0].rejectionReason").doesNotExist());

        mockMvc.perform(get(URL).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(pendingId));

        mockMvc.perform(get(URL).param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(publishedId));

        mockMvc.perform(get(URL).param("eventId", eventId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get(URL).param("eventId", String.valueOf(eventId + 999)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get(URL).param("from", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(pendingId));
    }

    @Test
    public void testGetComments_shouldSortNewestFirst() throws Exception {
        long firstId = getIdFromObject(createComment("Первый"));
        long secondId = getIdFromObject(createComment("Второй"));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(secondId))
                .andExpect(jsonPath("$.content[1].id").value(firstId));
    }

    @Test
    public void testUpdateStatusPublished_shouldMakeCommentPublic() throws Exception {
        long commentId = getIdFromObject(createComment("Хороший комментарий"));

        moderate(commentId, CommentStatus.PUBLISHED, null)
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.rejectionReason").doesNotExist());

        mockMvc.perform(get("/comments/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(commentId));
    }

    @Test
    public void testUpdateStatusRejected_shouldStoreReasonAndHideFromPublic() throws Exception {
        long commentId = getIdFromObject(createComment("Спам"));

        moderate(commentId, CommentStatus.REJECTED, "Содержит спам")
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Содержит спам"));

        mockMvc.perform(get("/comments/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get(URL).param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].rejectionReason").value("Содержит спам"));
    }

    @Test
    public void testUpdateStatusWithPendingTarget_shouldReturnBadRequest() throws Exception {
        long commentId = getIdFromObject(createComment("Комментарий"));

        moderate(commentId, CommentStatus.PENDING, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Target status must be PUBLISHED or REJECTED"));
    }

    @Test
    public void testUpdateStatusWhenAlreadyModerated_shouldReturnConflict() throws Exception {
        long commentId = getIdFromObject(createComment("Комментарий"));
        moderate(commentId, CommentStatus.PUBLISHED, null);

        moderate(commentId, CommentStatus.REJECTED, "Передумали")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only comments with status PENDING can be moderated"));
    }

    @Test
    public void testUpdateStatusForUnknownComment_shouldReturnNotFound() throws Exception {
        moderate(999L, CommentStatus.PUBLISHED, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    public void testDelete_shouldRemoveCommentFromBothLists() throws Exception {
        long commentId = getIdFromObject(createComment("Комментарий"));
        moderate(commentId, CommentStatus.PUBLISHED, null);

        mockMvc.perform(delete(URL + "/" + commentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/comments/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    public void testDeleteForUnknownComment_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete(URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
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

    private ResultActions moderate(Long commentId, CommentStatus status, String rejectionReason) throws Exception {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(status);
        request.setRejectionReason(rejectionReason);

        return mockMvc.perform(patch(URL + "/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
