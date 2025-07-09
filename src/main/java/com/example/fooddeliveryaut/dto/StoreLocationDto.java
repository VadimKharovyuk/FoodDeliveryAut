package com.example.fooddeliveryaut.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
// === DTO для интеграции с сервисом магазинов ===

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreLocationDto {
    private Long storeId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
}