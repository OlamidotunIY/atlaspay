package com.atlaspay.shared.dto;

public record ApiResponse<T>(
    boolean status,
    String message,
    T data,
    Meta meta
) {
    public record Meta(long total, int skipped, int perPage, int page, int pageCount) {}
}
