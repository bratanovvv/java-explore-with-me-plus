package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.utils.formats.DateFormats;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {

    private Long id;
    private Long event;
    private Long requester;
    @JsonFormat(pattern = DateFormats.DATE_TIME_ISO_MILLIS)
    private LocalDateTime created;
    private RequestStatus status;
}
