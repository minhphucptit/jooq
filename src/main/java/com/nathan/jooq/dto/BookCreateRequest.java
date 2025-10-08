package com.nathan.jooq.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookCreateRequest(
        String title,
        String isbn,
        Integer publishedYear,
        BigDecimal price,
        List<AuthorAssignment> authors // list có authorId + contribution
) {}
