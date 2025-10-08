package com.nathan.jooq.dto;

public record AuthorRankingDTO(
        Long id,
        String name,
        String country,
        Double totalValue,
        Integer rank
) {}
