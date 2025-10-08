package com.nathan.jooq.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBookPriceRequest {
    private String authorName;
    private String authorCountry;
    private BigDecimal percent;
}
