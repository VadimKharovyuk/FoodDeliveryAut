package com.example.fooddeliveryaut.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 📊 DTO для краткого статуса геолокации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStatusDto {
    private Boolean hasLocation;
    private LocalDateTime lastUpdated;
    private String city;
    private String country;
    private String shortAddress;
}
