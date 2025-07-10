package com.example.fooddeliveryaut.controller;
import com.example.fooddeliveryaut.dto.*;
import com.example.fooddeliveryaut.service.UserLocationService;
import com.example.fooddeliveryaut.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserLocationController {

    private final UserLocationService userLocationService;
    private final JwtUtil jwtUtil;


    /**
     * 📍 Получение текущей геолокации пользователя
     */
    @GetMapping("/me/location")
    public ResponseEntity<ApiResponse<UserLocationDto>> getUserLocation(
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);

            log.debug("📍 Запрос геолокации от пользователя {}", userId);

            UserLocationDto locationDto = userLocationService.getUserLocation(userId);

            return ResponseEntity.ok(
                    ApiResponse.success(locationDto,
                            locationDto.getHasLocation() ? "Геолокация найдена" : "Геолокация не установлена")
            );

        } catch (Exception e) {
            log.error("❌ Ошибка получения геолокации: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка получения геолокации: " + e.getMessage()));
        }
    }


    /**
     * 📍 Обновление геолокации пользователя (координаты от браузера)
     */
    @PutMapping("/me/location")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserLocation(
//            @Valid
            @RequestBody UpdateUserLocationDto locationDto,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);

            log.info("🌍 Запрос обновления геолокации от пользователя {}: [{}, {}]",
                    userId, locationDto.getLatitude(), locationDto.getLongitude());

            UserResponseDto updatedUser = userLocationService.updateUserLocation(userId, locationDto);

            return ResponseEntity.ok(
                    ApiResponse.success(updatedUser, "Геолокация успешно обновлена")
            );

        } catch (Exception e) {
            log.error("❌ Ошибка обновления геолокации: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка обновления геолокации: " + e.getMessage()));
        }
    }

    /**
     * 🏠 Обновление адреса пользователя с геокодированием
     */
    @PutMapping("/me/address")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserAddress(
            @Valid @RequestBody UpdateUserAddressDto addressDto,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);

            log.info("🏠 Запрос обновления адреса от пользователя {}: {}, {}",
                    userId, addressDto.getStreet(), addressDto.getCity());

            UserResponseDto updatedUser = userLocationService.updateUserAddress(userId, addressDto);

            return ResponseEntity.ok(
                    ApiResponse.success(updatedUser, "Адрес успешно обновлен")
            );

        } catch (Exception e) {
            log.error("❌ Ошибка обновления адреса: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка обновления адреса: " + e.getMessage()));
        }
    }



    /**
     * 🧹 Очистка геолокации пользователя
     */
    @DeleteMapping("/me/location")
    public ResponseEntity<ApiResponse<UserResponseDto>> clearUserLocation(
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);

            log.info("🧹 Запрос очистки геолокации от пользователя {}", userId);

            UserResponseDto updatedUser = userLocationService.clearUserLocation(userId);

            return ResponseEntity.ok(
                    ApiResponse.success(updatedUser, "Геолокация успешно очищена")
            );

        } catch (Exception e) {
            log.error("❌ Ошибка очистки геолокации: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка очистки геолокации: " + e.getMessage()));
        }
    }


    /**
     * 🔍 Поиск ближайших магазинов к пользователю (с параметрами)
     */
    @PostMapping("/me/nearby-stores")
    public ResponseEntity<ApiResponse<List<NearbyStoreDto>>> findNearbyStores(
            @Valid @RequestBody FindNearbyStoresDto searchDto,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);
            long startTime = System.currentTimeMillis(); // Для замера времени

            log.info("🔍 Запрос поиска магазинов от пользователя {} в радиусе {} км",
                    userId, searchDto.getRadiusKm());

            List<NearbyStoreDto> nearbyStores = userLocationService.findNearbyStores(userId, searchDto);

            String message = String.format("Найдено %d магазинов в радиусе %d км",
                    nearbyStores.size(), searchDto.getRadiusKm());

            // 🌍 Создаем специальные метаданные для геолокационного поиска
            ApiMetadata  metadata = ApiMetadata.nearbyStores(
                    nearbyStores.size(),                    // количество найденных
                    searchDto.getRadiusKm().doubleValue(),  // радиус поиска
                    "Пользователь " + userId,              // центр поиска
                    "distance",                            // сортировка по расстоянию
                    buildFilterString(searchDto)           // строка фильтров
            ).withExecutionTime(System.currentTimeMillis() - startTime);

            return ResponseEntity.ok(
                    ApiResponse.success(nearbyStores, message, metadata)
            );

        } catch (Exception e) {
            log.error("❌ Ошибка поиска магазинов: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка поиска магазинов: " + e.getMessage()));
        }
    }

    /**
     * 🔍 Упрощенный поиск ближайших магазинов (GET с query параметрами)
     */
    @GetMapping("/me/nearby-stores")
    public ResponseEntity<ApiResponse<List<NearbyStoreDto>>> getNearbyStores(
            @RequestParam(defaultValue = "10") Integer radiusKm,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "true") Boolean onlyOpen,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);
            long startTime = System.currentTimeMillis();

            log.info("🔍 Простой запрос поиска магазинов от пользователя {} (радиус: {} км)",
                    userId, radiusKm);

            // Создаем DTO для поиска с параметрами из query
            FindNearbyStoresDto searchDto = FindNearbyStoresDto.builder()
                    .radiusKm(radiusKm)
                    .limit(limit)
                    .category(category)
                    .minRating(minRating)
                    .onlyOpen(onlyOpen)
                    .build();

            List<NearbyStoreDto> nearbyStores = userLocationService.findNearbyStores(userId, searchDto);

            String message = String.format("Найдено %d магазинов в радиусе %d км",
                    nearbyStores.size(), radiusKm);

            // 🌍 Более простые метаданные для GET запроса
            ApiMetadata metadata = ApiMetadata.geoSearch(
                            nearbyStores.size(),
                            radiusKm.doubleValue(),
                            "Пользователь " + userId
                    ).withExecutionTime(System.currentTimeMillis() - startTime)
                    .withFilter(buildSimpleFilterString(category, minRating, onlyOpen));

            return ResponseEntity.ok(
                    ApiResponse.success(nearbyStores, message, metadata)
            );

        } catch (Exception e) {
            log.error("❌ Ошибка получения ближайших магазинов: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка получения магазинов: " + e.getMessage()));
        }
    }

    /**
     * 📏 Расчет расстояния до конкретного магазина
     */
    @GetMapping("/me/distance-to-store/{storeId}")
    public ResponseEntity<ApiResponse<DistanceCalculationDto>> getDistanceToStore(
            @PathVariable Long storeId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);
            long startTime = System.currentTimeMillis();

            log.debug("📏 Запрос расчета расстояния от пользователя {} до магазина {}", userId, storeId);

            DistanceCalculationDto distanceDto = userLocationService.calculateDistanceToStore(userId, storeId);

            String message = String.format("Расстояние до магазина '%s': %s",
                    distanceDto.getStoreName(), distanceDto.getDistanceText());

            // 📏 Метаданные для расчета расстояния
            ApiMetadata  metadata = ApiMetadata.withCount(1)
                    .withExecutionTime(System.currentTimeMillis() - startTime);

            return ResponseEntity.ok(
                    ApiResponse.success(distanceDto, message, metadata)
            );

        } catch (Exception e) {
            log.error("❌ Ошибка расчета расстояния: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка расчета расстояния: " + e.getMessage()));
        }
    }


    /**
     * 📊 Статистика по геолокации пользователей (только для админов)
     */
    @GetMapping("/location-stats")
    public ResponseEntity<ApiResponse<UserLocationStatsDto>> getLocationStats(
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);
            long startTime = System.currentTimeMillis();

            // TODO: Добавить проверку на роль админа
            // String userRole = getUserRoleFromToken(authHeader);
            // if (!"ADMIN".equals(userRole)) {
            //     return ResponseEntity.status(HttpStatus.FORBIDDEN)
            //             .body(ApiResponse.error("Доступ запрещен. Требуются права администратора."));
            // }

            log.info("📊 Запрос статистики геолокации от пользователя {}", userId);

            UserLocationStatsDto stats = userLocationService.getUserLocationStats();

            String message = String.format("Статистика: %d пользователей (%.1f%% с геолокацией)",
                    stats.getTotalUsers(), stats.getLocationCoverage());

            // 📊 Метаданные для статистики
            ApiMetadata metadata = ApiMetadata.withTotalCount(
                            stats.getTopCities().size(),           // количество городов в ответе
                            stats.getTotalUsers()                  // общее количество пользователей
                    ).withExecutionTime(System.currentTimeMillis() - startTime)
                    .withVersion("v1.0")
                    .withSort("userCount", "DESC");

            return ResponseEntity.ok(
                    ApiResponse.success(stats, message, metadata)
            );

        } catch (Exception e) {
            log.error("❌ Ошибка получения статистики: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка получения статистики: " + e.getMessage()));
        }
    }

    /**
     * 🆔 Проверка состояния геолокации
     */
    @GetMapping("/me/location/status")
    public ResponseEntity<ApiResponse<LocationStatusDto>> getLocationStatus(
            @RequestHeader("Authorization") String authHeader) {

        try {
            Long userId = getUserIdFromToken(authHeader);

            UserLocationDto location = userLocationService.getUserLocation(userId);

            LocationStatusDto status = LocationStatusDto.builder()
                    .hasLocation(location.getHasLocation())
                    .lastUpdated(location.getLocationUpdatedAt())
                    .city(location.getCity())
                    .country(location.getCountry())
                    .shortAddress(location.getShortAddress())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success(status, "Статус геолокации получен")
            );

        } catch (Exception e) {
            log.error("❌ Ошибка получения статуса геолокации: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Ошибка получения статуса: " + e.getMessage()));
        }
    }

    // === UTILITY МЕТОДЫ ===

    /**
     * 🔐 Извлечение ID пользователя из JWT токена
     * Использует ваш существующий JwtUtil
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Некорректный формат Authorization header");
        }

        String token = authHeader.substring(7); // Убираем "Bearer "

        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Недействительный или истекший токен");
        }

        return jwtUtil.getUserIdFromToken(token);
    }



    /**
     * 🔍 Создание строки фильтров для сложного поиска
     */
    private String buildFilterString(FindNearbyStoresDto searchDto) {
        StringBuilder filters = new StringBuilder();

        if (searchDto.getCategory() != null) {
            filters.append("category:").append(searchDto.getCategory());
        }

        if (searchDto.getMinRating() != null) {
            if (filters.length() > 0) filters.append(", ");
            filters.append("minRating:").append(searchDto.getMinRating());
        }

        if (Boolean.TRUE.equals(searchDto.getOnlyOpen())) {
            if (filters.length() > 0) filters.append(", ");
            filters.append("onlyOpen:true");
        }

        if (searchDto.getLimit() != null) {
            if (filters.length() > 0) filters.append(", ");
            filters.append("limit:").append(searchDto.getLimit());
        }

        return filters.length() > 0 ? filters.toString() : null;
    }

    /**
     * 🔍 Создание строки фильтров для простого поиска
     */
    private String buildSimpleFilterString(String category, Double minRating, Boolean onlyOpen) {
        StringBuilder filters = new StringBuilder();

        if (category != null) {
            filters.append("category:").append(category);
        }

        if (minRating != null) {
            if (filters.length() > 0) filters.append(", ");
            filters.append("minRating:").append(minRating);
        }

        if (Boolean.TRUE.equals(onlyOpen)) {
            if (filters.length() > 0) filters.append(", ");
            filters.append("onlyOpen:true");
        }

        return filters.length() > 0 ? filters.toString() : null;
    }

    /**
     * ❌ Обработчик исключений контроллера
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        log.error("❌ Ошибка в UserLocationController: {}", e.getMessage());

        // Определяем HTTP статус на основе сообщения ошибки
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "GENERAL_ERROR";

        if (e.getMessage().contains("не найден")) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "NOT_FOUND";
        } else if (e.getMessage().contains("токен") || e.getMessage().contains("авторизован")) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "UNAUTHORIZED";
        } else if (e.getMessage().contains("доступ запрещен")) {
            status = HttpStatus.FORBIDDEN;
            errorCode = "FORBIDDEN";
        } else if (e.getMessage().contains("геолокация не установлена")) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "LOCATION_NOT_SET";
        }

        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getMessage(), errorCode));
    }

    /**
     * ❌ Обработчик ошибок валидации
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Ошибка валидации: " + errorMessage, "VALIDATION_ERROR"));
    }
}

