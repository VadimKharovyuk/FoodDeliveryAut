package com.example.fooddeliveryaut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 📅 DTO для распределения обновлений по времени
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdatePeriodDto {
    private String period; // "Сегодня", "На этой неделе", etc.
    private Long userCount;
    private Double percentage;
}
