package com.nathan.jooq.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AuthorDTO(
        Long id,
        String name,
        String email,
        LocalDate birthDate,
        String country,
        LocalDateTime createdAt,
        String contribution
) {}
