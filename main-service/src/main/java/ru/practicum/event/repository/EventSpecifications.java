package ru.practicum.event.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.event.dto.query.AdminEventFilter;
import ru.practicum.event.dto.query.PublicEventFilter;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Спецификации поиска событий.
 */
public final class EventSpecifications {

    private EventSpecifications() {
    }

    /**
     * Публичный поиск: только опубликованные события и заданные фильтры.
     */
    public static Specification<Event> publicEvents(PublicEventFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            String text = normalizeText(filter.text());
            if (text != null) {
                String pattern = "%" + text.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("annotation")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            if (isNotEmpty(filter.categories())) {
                predicates.add(root.get("category").get("id").in(filter.categories()));
            }
            if (filter.paid() != null) {
                predicates.add(cb.equal(root.get("paid"), filter.paid()));
            }
            if (filter.rangeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), filter.rangeStart()));
            }
            if (filter.rangeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), filter.rangeEnd()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Административный поиск: все состояния, фильтры применяются при наличии.
     */
    public static Specification<Event> adminEvents(AdminEventFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (isNotEmpty(filter.users())) {
                predicates.add(root.get("initiator").get("id").in(filter.users()));
            }
            if (isNotEmpty(filter.states())) {
                predicates.add(root.get("state").in(filter.states()));
            }
            if (isNotEmpty(filter.categories())) {
                predicates.add(root.get("category").get("id").in(filter.categories()));
            }
            if (filter.rangeStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), filter.rangeStart()));
            }
            if (filter.rangeEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), filter.rangeEnd()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean isNotEmpty(List<?> values) {
        return values != null && !values.isEmpty();
    }

    private static String normalizeText(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
