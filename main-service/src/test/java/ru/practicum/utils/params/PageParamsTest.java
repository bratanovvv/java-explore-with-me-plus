package ru.practicum.utils.params;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageParamsTest {

    @Test
    void toPageable_withMultipleFrom_shouldUseExactOffset() {
        PageParams params = new PageParams(20, 10);

        Pageable pageable = params.toPageable(Sort.by("id"));

        assertThat(pageable.getOffset()).isEqualTo(20);
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).isEqualTo(Sort.by("id"));
    }

    @Test
    void toPageable_withNonMultipleFrom_shouldKeepExactOffset() {
        PageParams params = new PageParams(15, 10);

        Pageable pageable = params.toPageable(Sort.by("id"));

        assertThat(pageable.getOffset()).isEqualTo(15);
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void toPageable_withoutSort_shouldBeUnsorted() {
        PageParams params = new PageParams(0, 10);

        Pageable pageable = params.toPageable();

        assertThat(pageable.getSort()).isEqualTo(Sort.unsorted());
    }

    @Test
    void constructor_withNegativeFrom_shouldThrow() {
        assertThatThrownBy(() -> new PageParams(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from");
    }

    @Test
    void constructor_withNonPositiveSize_shouldThrow() {
        assertThatThrownBy(() -> new PageParams(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }
}
