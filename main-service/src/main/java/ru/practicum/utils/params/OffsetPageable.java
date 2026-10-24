package ru.practicum.utils.params;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * {@link Pageable} с точным смещением: {@code offset} — количество пропускаемых элементов, а не номер страницы.
 * <p>

 */
public class OffsetPageable implements Pageable {

    private final long offset;
    private final int size;
    private final Sort sort;

    public OffsetPageable(long offset, int size, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        this.offset = offset;
        this.size = size;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / size);
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageable(offset + size, size, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetPageable(offset - size, size, sort) : this;
    }

    @Override
    public Pageable first() {
        return new OffsetPageable(0, size, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be greater than or equal to 0");
        }
        return new OffsetPageable((long) pageNumber * size, size, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }

    @Override
    public String toString() {
        return "OffsetPageable(offset=" + offset + ", size=" + size + ", sort=" + sort + ")";
    }
}
