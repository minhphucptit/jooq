package com.nathan.jooq.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookDTO(
        Long id,
        String title,
        String isbn,
        Integer publishedYear,
        BigDecimal price,
        LocalDateTime createdAt,
        List<AuthorDTO> authors
) {
}
