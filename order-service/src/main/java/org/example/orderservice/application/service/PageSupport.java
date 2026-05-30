package org.example.orderservice.application.service;

import org.example.orderservice.application.dto.response.PageResponse;
import org.example.orderservice.domain.exceptions.DomainValidationException;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

final class PageSupport {

    private static final int MAX_PAGE_SIZE = 100;

    private PageSupport() {
    }

    static <T, R> PageResponse<R> page(Stream<T> source, int page, int size, String sortBy,
                                       String sortDirection, Comparator<T> comparator, Function<T, R> mapper) {
        validate(page, size, sortDirection);
        Comparator<T> selectedComparator = "desc".equalsIgnoreCase(sortDirection)
                ? comparator.reversed()
                : comparator;
        List<T> items = source.sorted(selectedComparator).toList();
        int fromIndex = (int) Math.min((long) page * size, items.size());
        int toIndex = Math.min(fromIndex + size, items.size());
        List<R> content = items.subList(fromIndex, toIndex).stream().map(mapper).toList();
        int totalPages = items.isEmpty() ? 0 : (items.size() + size - 1) / size;
        return new PageResponse<>(content, page, size, items.size(), totalPages, sortBy, sortDirection.toLowerCase());
    }

    private static void validate(int page, int size, String sortDirection) {
        if (page < 0) {
            throw new DomainValidationException("Page must not be negative");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new DomainValidationException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!"asc".equalsIgnoreCase(sortDirection) && !"desc".equalsIgnoreCase(sortDirection)) {
            throw new DomainValidationException("Sort direction must be asc or desc");
        }
    }
}
