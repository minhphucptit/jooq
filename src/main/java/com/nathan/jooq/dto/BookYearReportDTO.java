package com.nathan.jooq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookYearReportDTO {
    private Integer publishedYear;
    private Integer totalBooks;
    private BigDecimal totalValue;
}

