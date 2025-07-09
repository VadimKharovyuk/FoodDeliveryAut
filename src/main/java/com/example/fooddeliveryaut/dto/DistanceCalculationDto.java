package com.example.fooddeliveryaut.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 📏 DTO для расчета расстояния
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceCalculationDto {
    private Long storeId;
    private String storeName;
    private Double distanceKm;
    private String distanceText;
    private Integer estimatedDeliveryTime;
    private BigDecimal deliveryFee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculatedAt;
}
