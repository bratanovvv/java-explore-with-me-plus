package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.mapper.impl.EndpointHitMapper;
import ru.practicum.stats.mapper.impl.ViewStatsMapper;
import ru.practicum.stats.model.EndpointHit;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsService statsService;

    @MockBean
    private EndpointHitMapper endpointHitMapper;

    @MockBean
    private ViewStatsMapper viewStatsMapper;

    @Test
    void hit_shouldReturn201() throws Exception {
        when(endpointHitMapper.toEntity(any(EndpointHitDto.class))).thenReturn(new EndpointHit());

        postHit(hitDto("ewm-main-service")).andExpect(status().isCreated());

        verify(endpointHitMapper).toEntity(any(EndpointHitDto.class));
        verify(statsService).saveHit(any(EndpointHit.class));
    }

    @Test
    void hit_withBlankApp_shouldReturn400() throws Exception {
        postHit(hitDto(" ")).andExpect(status().isBadRequest());
    }

    @Test
    void getStats_shouldReturn200() throws Exception {
        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), isNull(), eq(true)))
                .thenReturn(List.of());
        when(viewStatsMapper.toDtoList(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", "2026-10-20 00:00:00")
                        .param("end", "2026-10-21 00:00:00")
                        .param("unique", "true"))
                .andExpect(status().isOk());

        verify(statsService).getStats(any(LocalDateTime.class), any(LocalDateTime.class), isNull(), eq(true));
        verify(viewStatsMapper).toDtoList(anyList());
    }

    private static EndpointHitDto hitDto(String app) {
        return new EndpointHitDto(null, app, "/events/1", "1.1.1.1", LocalDateTime.now());
    }

    private ResultActions postHit(EndpointHitDto dto) throws Exception {
        return mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }
}
