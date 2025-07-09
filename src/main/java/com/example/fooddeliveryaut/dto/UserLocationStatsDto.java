package com.example.fooddeliveryaut.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 📊 DTO для статистики геолокации (для админов)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationStatsDto {
    private Long totalUsers;
    private Long usersWithLocation;
    private Long usersWithoutLocation;
    private Double locationCoverage; // процент пользователей с геолокацией

    // 📈 Статистика по городам
    private List<CityStatsDto> topCities;

    // 📅 Статистика по времени
    private Long locationsUpdatedToday;
    private Long locationsUpdatedThisWeek;
    private Long locationsUpdatedThisMonth;
}