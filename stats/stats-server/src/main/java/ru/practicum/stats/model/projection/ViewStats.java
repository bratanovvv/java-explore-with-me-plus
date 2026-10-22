package ru.practicum.stats.model.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ViewStats {

    private final String app;

    private final String uri;

    private final Long hits;
}
