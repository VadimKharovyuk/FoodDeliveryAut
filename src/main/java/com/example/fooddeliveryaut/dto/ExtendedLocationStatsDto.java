package com.example.fooddeliveryaut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//**
//        * 📊 Расширенная статистика геолокации
// */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtendedLocationStatsDto {
    // Основная статистика
    private Long totalUsers;
    private Long usersWithLocation;
    private Long usersWithoutLocation;
    private Double locationCoverage;

    // Статистика по времени
    private Long locationsUpdatedToday;
    private Long locationsUpdatedThisWeek;
    private Long locationsUpdatedThisMonth;
    private Long locationsUpdatedLastMonth;

    // Топ регионы
    private java.util.List<CityStatsDto> topCities;
    private java.util.List<CountryStatsDto> topCountries;

    // Распределение по времени обновления
    private java.util.List<LocationUpdatePeriodDto> updateDistribution;

    // Географические данные
    private java.util.List<CityDistanceDto> cityDistances;

    // Дополнительные метрики
    private Double averageUpdatesPerUser;
    private String mostActiveCity;
    private String mostActiveCountry;
}