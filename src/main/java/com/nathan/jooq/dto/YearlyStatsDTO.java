package com.nathan.jooq.dto;

import java.math.BigDecimal;

public record YearlyStatsDTO(Integer year, Integer totalBooks, BigDecimal avgPrice) {}
