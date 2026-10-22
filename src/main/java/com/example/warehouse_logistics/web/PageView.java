package com.example.warehouse_logistics.web;

import java.util.List;

public record PageView<T>(List<T> items, int page, int size, int totalPages, long total) {

    public static <T> PageView<T> of(List<T> all, Integer page, Integer size) {
        int safeSize = size == null ? 20 : Math.max(1, Math.min(size, 100));
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) safeSize));
        int requested = page == null ? 1 : page;
        int safePage = Math.max(1, Math.min(requested, totalPages));
        int from = (safePage - 1) * safeSize;
        int to = Math.min(from + safeSize, all.size());
        return new PageView<>(all.subList(from, to), safePage, safeSize, totalPages, all.size());
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
