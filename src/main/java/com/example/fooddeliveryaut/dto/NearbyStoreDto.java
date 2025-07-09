package com.example.fooddeliveryaut.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/// 🏪 DTO для результата поиска магазинов с расстоянием
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyStoreDto {
    private Long storeId;
    private String name;
    private String description;
    private String category;
    private String imageUrl;

    // 📍 Координаты магазина
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String fullAddress;

    // 📊 Рейтинг и статус
    private Double rating;
    private Integer reviewCount;
    private Boolean isOpen;
    private String workingHours;

    // 📏 Расстояние и доставка
    private Double distanceKm;
    private String distanceText; // "1.2 км" или "850 м"
    private Integer estimatedDeliveryTime; // в минутах
    private BigDecimal deliveryFee;

    // 💰 Минимальная сумма заказа
    private BigDecimal minOrderAmount;


}
