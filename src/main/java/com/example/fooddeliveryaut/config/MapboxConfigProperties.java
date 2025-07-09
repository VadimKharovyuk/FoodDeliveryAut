package com.example.fooddeliveryaut.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 🗺️ Конфигурация для Mapbox Geocoding API
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mapbox")
public class MapboxConfigProperties {

    /**
     * 🔑 Токен доступа к Mapbox API
     */
    private Access access = new Access();

    /**
     * 🌍 Настройки геокодирования
     */
    private Geocoding geocoding = new Geocoding();

    @Data
    public static class Access {
        /**
         * Основной токен доступа
         */
        private String token;
    }

    @Data
    public static class Geocoding {
        /**
         * Лимит результатов поиска
         */
        private Integer limit = 1;

        /**
         * Типы результатов (address, poi, etc.)
         */
        private String types = "address,poi";

        /**
         * Код страны для ограничения поиска
         */
        private String country = "RU";

        /**
         * Тайм-аут подключения (мс)
         */
        private Integer connectionTimeout = 5000;

        /**
         * Тайм-аут чтения (мс)
         */
        private Integer readTimeout = 10000;

        /**
         * Включено ли геокодирование
         */
        private Boolean enabled = true;
    }

    // === UTILITY МЕТОДЫ ===

    /**
     * Проверка наличия токена
     */
    public boolean hasValidToken() {
        return access.token != null &&
                !access.token.trim().isEmpty() &&
                access.token.startsWith("pk.");
    }

    /**
     * Получение полного URL для геокодирования
     */
    public String getGeocodingUrl() {
        return "https://api.mapbox.com/geocoding/v5/mapbox.places";
    }

    /**
     * Получение параметров для URL
     */
    public String getDefaultParams() {
        StringBuilder params = new StringBuilder();
        params.append("access_token=").append(access.token);
        params.append("&limit=").append(geocoding.limit);
        params.append("&types=").append(geocoding.types);

        if (geocoding.country != null && !geocoding.country.isEmpty()) {
            params.append("&country=").append(geocoding.country);
        }

        return params.toString();
    }
}