package com.example.fooddeliveryaut.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMetadata {

    /**
     * 📄 Пагинация
     */
    private Integer page;           // Текущая страница (начиная с 0)
    private Integer size;           // Размер страницы
    private Integer totalPages;     // Общее количество страниц
    private Long totalElements;     // Общее количество элементов

    /**
     * 📊 Подсчет элементов
     */
    private Integer count;          // Количество элементов в текущем ответе
    private Long totalCount;        // Общее количество элементов

    /**
     * 🔍 Информация о фильтрации/поиске
     */
    private String searchQuery;     // Поисковый запрос
    private String sortBy;          // Поле сортировки
    private String sortDirection;   // Направление сортировки (ASC/DESC)
    private String filterBy;        // Примененные фильтры

    /**
     * ⏱️ Производительность
     */
    private Long executionTimeMs;   // Время выполнения в миллисекундах

    /**
     * 📍 Дополнительная информация
     */
    private String version;         // Версия API
    private String requestId;       // ID запроса для трассировки

    /**
     * 🌍 Геолокация (специфично для поиска магазинов)
     */
    private Double radiusKm;        // Радиус поиска в километрах
    private String centerLocation;  // Центр поиска (координаты или адрес)

    // === СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ МЕТАДАННЫХ ===

    /**
     * 📄 Создание метаданных для пагинации
     */
    public static ApiMetadata pagination(int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return ApiMetadata.builder()
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }

    /**
     * 📊 Создание метаданных с подсчетом
     */
    public static ApiMetadata withCount(int count) {
        return ApiMetadata.builder()
                .count(count)
                .build();
    }

    /**
     * 📊 Создание метаданных с общим подсчетом
     */
    public static ApiMetadata withTotalCount(int count, long totalCount) {
        return ApiMetadata.builder()
                .count(count)
                .totalCount(totalCount)
                .build();
    }

    /**
     * 🔍 Создание метаданных для поиска
     */
    public static ApiMetadata search(String query, int count, String sortBy) {
        return ApiMetadata.builder()
                .searchQuery(query)
                .count(count)
                .sortBy(sortBy)
                .build();
    }

    /**
     * 🌍 Создание метаданных для геолокационного поиска
     */
    public static ApiMetadata geoSearch(int count, double radiusKm, String centerLocation) {
        return ApiMetadata.builder()
                .count(count)
                .radiusKm(radiusKm)
                .centerLocation(centerLocation)
                .build();
    }

    /**
     * 🌍 Создание метаданных для поиска ближайших магазинов
     */
    public static ApiMetadata nearbyStores(int count, double radiusKm, String userLocation,
                                           String sortBy, String filterBy) {
        return ApiMetadata.builder()
                .count(count)
                .radiusKm(radiusKm)
                .centerLocation(userLocation)
                .sortBy(sortBy != null ? sortBy : "distance")
                .filterBy(filterBy)
                .build();
    }

    // === МЕТОДЫ BUILDER PATTERN ===

    /**
     * ⏱️ Добавление времени выполнения
     */
    public ApiMetadata withExecutionTime(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
        return this;
    }

    /**
     * 🆔 Добавление ID запроса
     */
    public ApiMetadata withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * 📍 Добавление версии API
     */
    public ApiMetadata withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * 🔍 Добавление информации о фильтрах
     */
    public ApiMetadata withFilter(String filterBy) {
        this.filterBy = filterBy;
        return this;
    }

    /**
     * 📊 Добавление сортировки
     */
    public ApiMetadata withSort(String sortBy, String sortDirection) {
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        return this;
    }

    // === UTILITY МЕТОДЫ ===

    /**
     * 📄 Проверка наличия пагинации
     */
    public boolean hasPagination() {
        return page != null && size != null && totalPages != null;
    }

    /**
     * 🔍 Проверка наличия поиска
     */
    public boolean hasSearch() {
        return searchQuery != null && !searchQuery.trim().isEmpty();
    }

    /**
     * 🌍 Проверка наличия геолокационных данных
     */
    public boolean hasGeoData() {
        return radiusKm != null && centerLocation != null;
    }

    /**
     * ⏱️ Проверка наличия данных о производительности
     */
    public boolean hasPerformanceData() {
        return executionTimeMs != null;
    }
}