package ru.practicum.utils.params;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PageParamsTest {

    @Test
    void toPageable_withMultipleFrom_shouldUseWholePage() {
        PageParams params = new PageParams(20, 10);

        Pageable pageable = params.toPageable(Sort.by("id"));

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void toPageable_withNonMultipleFrom_shouldLoadOffsetInsidePage() {
        PageParams params = new PageParams(15, 10);

        Pageable pageable = params.toPageable(Sort.by("id"));

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(15);
    }

    @Test
    void cut_withMultipleFrom_shouldKeepWholePage() {
        PageParams params = new PageParams(20, 10);
        List<Integer> loaded = IntStream.rangeClosed(20, 29).boxed().toList();

        List<Integer> actual = params.cut(loaded);

        assertThat(actual).containsExactlyElementsOf(loaded);
    }

    @Test
    void cut_withNonMultipleFrom_shouldReturnRequestedWindow() {
        PageParams params = new PageParams(15, 10);
        List<Integer> loaded = IntStream.rangeClosed(10, 24).boxed().toList();

        List<Integer> actual = params.cut(loaded);

        assertThat(actual).containsExactly(15, 16, 17, 18, 19, 20, 21, 22, 23, 24);
    }

    @Test
    void cut_onLastPartialPage_shouldReturnAvailableItems() {
        PageParams params = new PageParams(15, 10);
        List<Integer> loaded = IntStream.rangeClosed(10, 17).boxed().toList();

        List<Integer> actual = params.cut(loaded);

        assertThat(actual).containsExactly(15, 16, 17);
    }
}
