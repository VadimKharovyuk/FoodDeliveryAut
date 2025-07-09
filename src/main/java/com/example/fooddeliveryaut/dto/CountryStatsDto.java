package com.example.fooddeliveryaut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🌍 DTO для статистики по странам
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryStatsDto {
    private String country;
    private Long userCount;
    private Double percentage;
}
