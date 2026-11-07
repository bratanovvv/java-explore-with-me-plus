package ru.practicum.utils.formats;

import lombok.experimental.UtilityClass;

/**
 * Форматы даты и времени, используемые в DTO и query-параметрах.
 */
@UtilityClass
public final class DateFormats {

    /**
     * Формат событий, статистики и query-параметров rangeStart/rangeEnd.
     */
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * Формат поля created в заявках на участие.
     */
    public static final String DATE_TIME_ISO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS";
}
