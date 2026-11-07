package ru.practicum.utils.params;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OffsetPageableTest {

    private static final Sort SORT = Sort.by(Sort.Direction.ASC, "id");

    @Test
    void constructor_shouldKeepOffsetAndSize() {
        OffsetPageable pageable = new OffsetPageable(15, 10, SORT);

        assertThat(pageable.getOffset()).isEqualTo(15);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getSort()).isEqualTo(SORT);
        assertThat(pageable.hasPrevious()).isTrue();
    }

    @Test
    void next_shouldShiftOffsetByPageSize() {
        Pageable next = new OffsetPageable(15, 10, SORT).next();

        assertThat(next.getOffset()).isEqualTo(25);
        assertThat(next.getPageSize()).isEqualTo(10);
    }

    @Test
    void previousOrFirst_withOffset_shouldShiftBack() {
        Pageable previous = new OffsetPageable(15, 10, SORT).previousOrFirst();

        assertThat(previous.getOffset()).isEqualTo(5);
    }

    @Test
    void previousOrFirst_withoutOffset_shouldReturnSameInstance() {
        OffsetPageable pageable = new OffsetPageable(0, 10, SORT);

        assertThat(pageable.previousOrFirst()).isSameAs(pageable);
        assertThat(pageable.hasPrevious()).isFalse();
    }

    @Test
    void first_shouldResetOffset() {
        assertThat(new OffsetPageable(15, 10, SORT).first().getOffset()).isZero();
    }

    @Test
    void withPage_shouldDeriveOffsetFromPageNumber() {
        assertThat(new OffsetPageable(15, 10, SORT).withPage(3).getOffset()).isEqualTo(30);
    }

    @Test
    void withPage_withNegativePageNumber_shouldThrow() {
        OffsetPageable pageable = new OffsetPageable(15, 10, SORT);

        assertThatThrownBy(() -> pageable.withPage(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_withNegativeOffset_shouldThrow() {
        assertThatThrownBy(() -> new OffsetPageable(-1, 10, SORT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_withNonPositiveSize_shouldThrow() {
        assertThatThrownBy(() -> new OffsetPageable(0, 0, SORT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_withNullSort_shouldUseUnsorted() {
        assertThat(new OffsetPageable(0, 10, null).getSort()).isEqualTo(Sort.unsorted());
    }
}