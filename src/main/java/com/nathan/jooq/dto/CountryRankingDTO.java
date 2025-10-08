package com.nathan.jooq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryRankingDTO {
    private String country;
    private BigDecimal totalValue;
    private Integer rank;
}

