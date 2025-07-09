package com.example.fooddeliveryaut.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 🔍 DTO для поиска ближайших магазинов
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindNearbyStoresDto {

    // Координаты (если не указаны, используются сохраненные)
    @DecimalMin(value = "-90.0", message = "Широта должна быть не меньше -90")
    @DecimalMax(value = "90.0", message = "Широта должна быть не больше 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Долгота должна быть не меньше -180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть не больше 180")
    private BigDecimal longitude;

    // 📏 Радиус поиска в километрах
    @Min(value = 1, message = "Радиус должен быть не меньше 1 км")
    @Max(value = 50, message = "Радиус должен быть не больше 50 км")
    @Builder.Default
    private Integer radiusKm = 10;

    // 📊 Количество результатов
    @Min(value = 1, message = "Лимит должен быть не меньше 1")
    @Max(value = 100, message = "Лимит должен быть не больше 100")
    @Builder.Default
    private Integer limit = 20;

    // 🔍 Фильтры
    @Size(max = 50, message = "Категория не должна превышать 50 символов")
    private String category;

    @DecimalMin(value = "0.0", message = "Рейтинг не может быть отрицательным")
    @DecimalMax(value = "5.0", message = "Рейтинг не может быть больше 5")
    private Double minRating;

    @Builder.Default
    private Boolean onlyOpen = true;

    // 🎯 Сортировка
    @Builder.Default
    private String sortBy = "distance"; // distance, rating, delivery_time
}
