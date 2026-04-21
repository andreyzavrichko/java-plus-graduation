package ru.practicum.ewm.sharing;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.io.Serializable;

@UtilityClass
public class PageableFactory {

    public static Pageable offset(int offset, int limit) {
        validate(offset, limit);
        return new OffsetPageable(offset, limit);
    }

    public static Pageable offset(int offset, int limit, Sort sort) {
        validate(offset, limit);
        return new OffsetPageable(offset, limit, sort);
    }

    private void validate(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException(
                    String.format("Offset must not be negative: %d", offset));
        }
        if (limit <= 0) {
            throw new IllegalArgumentException(
                    String.format("Limit must be positive: %d", limit));
        }
    }

    private record OffsetPageable(int offset, int limit, Sort sort) implements Pageable, Serializable {

        public OffsetPageable(int offset, int limit) {
            this(offset, limit, Sort.unsorted());
        }

        private OffsetPageable(int offset, int limit, Sort sort) {
            this.offset = offset;
            this.limit = limit;
            this.sort = sort != null ? sort : Sort.unsorted();
        }

        @Override
        public int getPageNumber() {
            return offset / limit;
        }

        @Override
        public int getPageSize() {
            return limit;
        }

        @Override
        public long getOffset() {
            return offset;
        }

        @Override
        @NonNull
        public Sort getSort() {
            return sort;
        }

        @Override
        @NonNull
        public Pageable next() {
            return new OffsetPageable(offset + limit, limit, sort);
        }

        @Override
        @NonNull
        public Pageable previousOrFirst() {
            return hasPrevious() ? new OffsetPageable(offset - limit, limit, sort) : first();
        }

        @Override
        @NonNull
        public Pageable first() {
            return new OffsetPageable(0, limit, sort);
        }

        @Override
        @NonNull
        public Pageable withPage(int pageNumber) {
            return new OffsetPageable(pageNumber * limit, limit, sort);
        }

        @Override
        public boolean hasPrevious() {
            return offset > 0;
        }
    }
}
