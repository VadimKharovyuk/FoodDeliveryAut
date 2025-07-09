package com.example.fooddeliveryaut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🏙️ DTO для расстояний между пользователями в городе
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDistanceDto {
    private String city;
    private Double averageDistanceKm;
    private Integer userCount;
}
