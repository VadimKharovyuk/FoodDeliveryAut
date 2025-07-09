package com.example.fooddeliveryaut.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 📍 Конфигурация настроек геолокации
 * Читает настройки из application.properties с префиксом app.location
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.location")
public class LocationConfigProperties {

    /**
     * 📏 Радиус поиска магазинов по умолчанию (км)
     */
    private Integer defaultRadiusKm = 10;

    /**
     * 📏 Максимальный радиус поиска (км)
     */
    private Integer maxRadiusKm = 50;

    /**
     * 📊 Максимальное количество результатов поиска
     */
    private Integer maxResults = 100;

    /**
     * 📍 Fallback координаты
     */
    private Fallback fallback = new Fallback();

    /**
     * 🚚 Настройки доставки
     */
    private Delivery delivery = new Delivery();

    @Data
    public static class Fallback {
        /**
         * Широта по умолчанию (центр Европы)
         */
        private BigDecimal latitude = new BigDecimal("50.0000");

        /**
         * Долгота по умолчанию (центр Европы)
         */
        private BigDecimal longitude = new BigDecimal("20.0000");
    }

    @Data
    public static class Delivery {
        /**
         * Базовое время доставки в минутах
         */
        private Integer baseTimeMinutes = 15;

        /**
         * Средняя скорость доставки в км/ч
         */
        private Integer speedKmh = 30;

        /**
         * Базовая стоимость доставки
         */
        private BigDecimal baseFee = new BigDecimal("50");

        /**
         * Стоимость доставки за километр
         */
        private BigDecimal feePerKm = new BigDecimal("10");
    }
}

