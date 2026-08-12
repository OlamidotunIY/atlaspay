package com.atlaspay.shared.util;

import java.util.List;

/**
 * A generic record for paginated results across all modules.
 */
public record PageResult<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages
) {
    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }
    
    public boolean hasPrevious() {
        return pageNumber > 0;
    }
}
