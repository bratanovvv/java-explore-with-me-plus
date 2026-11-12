package ru.practicum.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.comment.dto.request.CommentStatusUpdateRequest;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.service.AdminCommentService;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;
import ru.practicum.utils.error.exception.BadRequestException;
import ru.practicum.utils.error.exception.ConflictException;
import ru.practicum.utils.error.exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCommentController.class)
@Import(CommentMapper.class)
class AdminCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminCommentService adminCommentService;

    @Test
    void getComments_shouldReturn200AndList() throws Exception {
        Comment comment = buildComment();

        when(adminCommentService.getComments(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(comment)));

        mockMvc.perform(get("/admin/comments")
                        .param("status", "PENDING")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].eventId").value(10L))
                .andExpect(jsonPath("$.content[0].authorId").value(20L))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.content[0].rejectionReason").doesNotExist());
    }

    @Test
    void getComments_invalidPagination_shouldReturn400() throws Exception {
        mockMvc.perform(get("/admin/comments").param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminCommentService);
    }

    @Test
    void updateStatus_shouldReturn200AndDto() throws Exception {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.PUBLISHED);

        Comment updatedComment = buildComment();
        updatedComment.setStatus(CommentStatus.PUBLISHED);

        when(adminCommentService.updateStatus(eq(1L), any())).thenReturn(updatedComment);

        mockMvc.perform(patch("/admin/comments/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void updateStatus_withNullStatus_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/admin/comments/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminCommentService);
    }

    @Test
    void updateStatus_conflict_shouldReturn409() throws Exception {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.PUBLISHED);

        when(adminCommentService.updateStatus(eq(1L), any()))
                .thenThrow(new ConflictException("Only PENDING"));

        mockMvc.perform(patch("/admin/comments/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatus_badRequest_shouldReturn400() throws Exception {
        CommentStatusUpdateRequest request = new CommentStatusUpdateRequest();
        request.setStatus(CommentStatus.PENDING);

        when(adminCommentService.updateStatus(eq(1L), any()))
                .thenThrow(new BadRequestException("Target must be PUBLISHED or REJECTED"));

        mockMvc.perform(patch("/admin/comments/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/comments/{commentId}", 1L))
                .andExpect(status().isNoContent());
        verify(adminCommentService).delete(1L);
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new NotFoundException("Not found")).when(adminCommentService).delete(99L);

        mockMvc.perform(delete("/admin/comments/{commentId}", 99L))
                .andExpect(status().isNotFound());
    }

    private Comment buildComment() {
        return Comment.builder()
                .id(1L)
                .text("Текст")
                .status(CommentStatus.PENDING)
                .event(Event.builder().id(10L).build())
                .author(User.builder().id(20L).build())
                .build();
    }
}
