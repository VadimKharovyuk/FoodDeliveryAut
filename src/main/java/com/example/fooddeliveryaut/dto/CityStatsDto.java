package com.example.fooddeliveryaut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 🏙️ DTO для статистики по городам
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityStatsDto {
    private String city;
    private String country;
    private Long userCount;
    private Double percentage;
}
