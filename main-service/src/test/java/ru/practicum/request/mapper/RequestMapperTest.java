package ru.practicum.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

    private final RequestMapper requestMapper = new RequestMapper();

    @Test
    void toDto_shouldMapAllFields() {
        User requester = User.builder().id(2L).name("Иван").build();
        Event event = Event.builder().id(10L).title("Концерт").build();

        Request request = Request.builder()
                .id(1L)
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();

        RequestDto dto = requestMapper.toDto(request);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEvent()).isEqualTo(10L);
        assertThat(dto.getRequester()).isEqualTo(2L);
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void toEntity_shouldMapAllFields() {
        LocalDateTime now = LocalDateTime.now();
        RequestDto dto = RequestDto.builder()
                .id(1L)
                .event(10L)
                .requester(2L)
                .created(now)
                .status(RequestStatus.CONFIRMED)
                .build();

        Request request = requestMapper.toEntity(dto);

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getEvent()).isNotNull();
        assertThat(request.getEvent().getId()).isEqualTo(10L);
        assertThat(request.getRequester()).isNotNull();
        assertThat(request.getRequester().getId()).isEqualTo(2L);

        assertThat(request.getCreated()).isEqualTo(now);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
    }
}
