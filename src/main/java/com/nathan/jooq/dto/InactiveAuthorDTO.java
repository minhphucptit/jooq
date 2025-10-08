package com.nathan.jooq.dto;

public record InactiveAuthorDTO(
        Long id,
        String name,
        String country,
        Integer lastPublishedYear,
        String reason
) {}
