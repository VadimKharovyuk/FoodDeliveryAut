package com.example.fooddeliveryaut.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 📍 DTO для получения геолокации пользователя
// 📍 DTO для получения геолокации пользователя
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLocationDto {


    // В user-service (8081)
    @Digits(integer = 2, fraction = 15)  // Вместо fraction = 8
    private BigDecimal latitude;

    @Digits(integer = 3, fraction = 15)  // Вместо fraction = 8
    private BigDecimal longitude;

    private String street;
    private String city;
    private String region;
    private String country;
    private String postalCode;
    private String fullAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime locationUpdatedAt;

    private Boolean hasLocation;

    // 📊 Дополнительная информация
    private String formattedCoordinates;
    private String shortAddress; // Только город и страна
}