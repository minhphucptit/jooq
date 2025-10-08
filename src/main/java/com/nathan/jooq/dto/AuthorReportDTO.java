package com.nathan.jooq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorReportDTO {
    private Long id;
    private String name;
    private Integer totalBooks;
    private BigDecimal totalValue;
    private BigDecimal avgPrice;
}

