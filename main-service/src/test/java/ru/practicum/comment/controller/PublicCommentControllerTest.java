package ru.practicum.comment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.comment.dto.query.CommentSort;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.service.PublicCommentService;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;
import ru.practicum.utils.error.exception.NotFoundException;
import ru.practicum.utils.params.PageParams;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCommentController.class)
@Import(CommentMapper.class)
class PublicCommentControllerTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 11, 8, 20, 10);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicCommentService commentService;

    @Test
    void getComments_shouldReturnPublishedCommentsWithDefaults() throws Exception {
        when(commentService.getPublishedComments(1L, CommentSort.NEWEST, new PageParams(0, 10)))
                .thenReturn(new PageImpl<>(List.of(buildComment())));

        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].text").value("Отличное событие"))
                .andExpect(jsonPath("$.content[0].eventId").value(1))
                .andExpect(jsonPath("$.content[0].authorId").value(2))
                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.content[0].created").value("2026-11-08 20:10:00"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(commentService).getPublishedComments(1L, CommentSort.NEWEST, new PageParams(0, 10));
    }

    @Test
    void getComments_shouldPassSortAndPagination() throws Exception {
        when(commentService.getPublishedComments(1L, CommentSort.OLDEST, new PageParams(2, 5)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/comments/1")
                        .param("sort", "OLDEST")
                        .param("from", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(commentService).getPublishedComments(1L, CommentSort.OLDEST, new PageParams(2, 5));
    }

    @Test
    void getComments_shouldReturnNotFoundForUnpublishedEvent() throws Exception {
        when(commentService.getPublishedComments(99L, CommentSort.NEWEST, new PageParams(0, 10)))
                .thenThrow(new NotFoundException("Event with id=99 was not found"));

        mockMvc.perform(get("/comments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Event with id=99 was not found"));
    }

    @Test
    void getComments_shouldRejectNegativeFrom() throws Exception {
        mockMvc.perform(get("/comments/1").param("from", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        verify(commentService, never()).getPublishedComments(any(), any(), any());
    }

    @Test
    void getComments_shouldRejectNonPositiveSize() throws Exception {
        mockMvc.perform(get("/comments/1").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        verify(commentService, never()).getPublishedComments(any(), any(), any());
    }

    @Test
    void getComments_shouldRejectUnknownSort() throws Exception {
        mockMvc.perform(get("/comments/1").param("sort", "POPULAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));

        verify(commentService, never()).getPublishedComments(any(), any(), any());
    }

    private Comment buildComment() {
        return Comment.builder()
                .id(5L)
                .text("Отличное событие")
                .event(Event.builder().id(1L).build())
                .author(User.builder().id(2L).build())
                .created(CREATED)
                .status(CommentStatus.PUBLISHED)
                .build();
    }
}
