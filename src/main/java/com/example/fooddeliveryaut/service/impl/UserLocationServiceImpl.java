package com.example.fooddeliveryaut.service.impl;

import com.example.fooddeliveryaut.config.LocationConfigProperties;
import com.example.fooddeliveryaut.config.ServiceNamesProperties;
import com.example.fooddeliveryaut.dto.*;
import com.example.fooddeliveryaut.mapper.UserMapper;
import com.example.fooddeliveryaut.model.User;
import com.example.fooddeliveryaut.repository.UserRepository;
import com.example.fooddeliveryaut.service.UserLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLocationServiceImpl implements UserLocationService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RestTemplate restTemplate; // Должен быть @LoadBalanced
    private final LocationConfigProperties locationConfig;
    private final ServiceNamesProperties serviceNames;

    /**
     * 📍 Обновление координат пользователя
     */
    @Override
    @Transactional
    public UserResponseDto updateUserLocation(Long userId, UpdateUserLocationDto locationDto) {
        log.info("🌍 Обновление геолокации для пользователя ID: {}", userId);

        User user = findUserById(userId);

        // Обновляем координаты
        user.updateLocation(locationDto.getLatitude(), locationDto.getLongitude());

        // Если передан адрес, обновляем его
        if (locationDto.getStreet() != null) {
            updateUserAddressFields(user, locationDto);
        }

        // Если нет адреса, но включено геокодирование - получаем адрес по координатам
        else if (Boolean.TRUE.equals(locationDto.getAutoGeocode()) &&
                user.getFullAddress() == null) {
            performReverseGeocoding(user, locationDto.getLongitude(), locationDto.getLatitude());
        }

        User savedUser = userRepository.save(user);
        log.info("✅ Геолокация пользователя {} обновлена: [{}, {}]",
                userId, savedUser.getLatitude(), savedUser.getLongitude());

        return userMapper.toResponseDto(savedUser);
    }

    /**
     * 🏠 Обновление адреса пользователя с геокодированием
     */
    @Override
    @Transactional
    public UserResponseDto updateUserAddress(Long userId, UpdateUserAddressDto addressDto) {
        log.info("🏠 Обновление адреса для пользователя ID: {}", userId);

        User user = findUserById(userId);

        // Обновляем адрес
        user.setStreet(addressDto.getStreet());
        user.setCity(addressDto.getCity());
        user.setRegion(addressDto.getRegion());
        user.setCountry(addressDto.getCountry());
        user.setPostalCode(addressDto.getPostalCode());
        user.setFullAddress(user.getFormattedAddress());

        // Если включено автоматическое геокодирование
        if (Boolean.TRUE.equals(addressDto.getAutoGeocode())) {
            performForwardGeocoding(user);
        }

        User savedUser = userRepository.save(user);
        log.info("✅ Адрес пользователя {} обновлен: {}", userId, savedUser.getFullAddress());

        return userMapper.toResponseDto(savedUser);
    }

    /**
     * 📍 Получение геолокации пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public UserLocationDto getUserLocation(Long userId) {
        log.debug("📍 Получение геолокации для пользователя ID: {}", userId);

        User user = findUserById(userId);

        return UserLocationDto.builder()
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .street(user.getStreet())
                .city(user.getCity())
                .region(user.getRegion())
                .country(user.getCountry())
                .postalCode(user.getPostalCode())
                .fullAddress(user.getFullAddress())
                .locationUpdatedAt(user.getLocationUpdatedAt())
                .hasLocation(user.hasLocation())
                .formattedCoordinates(formatCoordinates(user.getLatitude(), user.getLongitude()))
                .shortAddress(formatShortAddress(user.getCity(), user.getCountry()))
                .build();
    }

    /**
     * 🔍 Поиск ближайших магазинов к пользователю
     */
    @Override
    @Transactional(readOnly = true)
    public List<NearbyStoreDto> findNearbyStores(Long userId, FindNearbyStoresDto searchDto) {
        log.info("🔍 Поиск ближайших магазинов для пользователя ID: {}", userId);

        User user = findUserById(userId);

        // Используем координаты пользователя, если они не переданы в запросе
        BigDecimal latitude = searchDto.getLatitude() != null ?
                searchDto.getLatitude() : user.getLatitude();
        BigDecimal longitude = searchDto.getLongitude() != null ?
                searchDto.getLongitude() : user.getLongitude();

        if (latitude == null || longitude == null) {
            throw new RuntimeException("Координаты пользователя не установлены. Добавьте геолокацию в профиле.");
        }

        // Обновляем DTO с координатами пользователя
        searchDto.setLatitude(latitude);
        searchDto.setLongitude(longitude);

        // Вызываем сервис магазинов для поиска
        List<NearbyStoreDto> nearbyStores = callStoreServiceForNearbyStores(searchDto);

        log.info("✅ Найдено {} магазинов в радиусе {} км от пользователя {}",
                nearbyStores.size(), searchDto.getRadiusKm(), userId);

        return nearbyStores;
    }

    /**
     * 📏 Расчет расстояния между пользователем и магазином
     */
    @Override
    @Transactional(readOnly = true)
    public DistanceCalculationDto calculateDistanceToStore(Long userId, Long storeId) {
        log.debug("📏 Расчет расстояния от пользователя {} до магазина {}", userId, storeId);

        User user = findUserById(userId);

        if (!user.hasLocation()) {
            throw new RuntimeException("Геолокация пользователя не установлена");
        }

        // Получаем координаты магазина через API
        StoreLocationDto  storeLocation = getStoreLocation(storeId);

        // Рассчитываем расстояние
        double distanceKm = calculateDistanceInKm(
                user.getLatitude(), user.getLongitude(),
                storeLocation.getLatitude(), storeLocation.getLongitude()
        );

        // Рассчитываем время доставки и стоимость
        int deliveryTime = calculateDeliveryTime(distanceKm);
        BigDecimal deliveryFee = calculateDeliveryFee(distanceKm);

        return DistanceCalculationDto.builder()
                .storeId(storeId)
                .storeName(storeLocation.getName())
                .distanceKm(distanceKm)
                .distanceText(formatDistance(distanceKm))
                .estimatedDeliveryTime(deliveryTime)
                .deliveryFee(deliveryFee)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 📊 Получение статистики по геолокации пользователей
     */
    @Override
    @Transactional(readOnly = true)
    public UserLocationStatsDto getUserLocationStats() {
        log.info("📊 Получение статистики по геолокации пользователей");

        long totalUsers = userRepository.count();
        long usersWithLocation = userRepository.countUsersWithLocation();
        long usersWithoutLocation = totalUsers - usersWithLocation;
        double locationCoverage = totalUsers > 0 ? (double) usersWithLocation / totalUsers * 100 : 0;

        // Статистика по времени
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime monthStart = now.minusMonths(1);

        long locationsUpdatedToday = userRepository.countUsersWithLocationUpdatedToday();
        long locationsUpdatedThisWeek = userRepository.countUsersWithLocationUpdatedThisWeek(weekStart);
        long locationsUpdatedThisMonth = userRepository.countUsersWithLocationUpdatedThisMonth(monthStart);

        // Топ городов
        List<CityStatsDto> topCities = userRepository.getCityStatistics().stream()
                .limit(10)
                .collect(Collectors.toList());

        return UserLocationStatsDto.builder()
                .totalUsers(totalUsers)
                .usersWithLocation(usersWithLocation)
                .usersWithoutLocation(usersWithoutLocation)
                .locationCoverage(locationCoverage)
                .topCities(topCities)
                .locationsUpdatedToday(locationsUpdatedToday)
                .locationsUpdatedThisWeek(locationsUpdatedThisWeek)
                .locationsUpdatedThisMonth(locationsUpdatedThisMonth)
                .build();
    }

    /**
     * 🧹 Очистка геолокации пользователя
     */
    @Override
    @Transactional
    public UserResponseDto clearUserLocation(Long userId) {
        log.info("🧹 Очистка геолокации для пользователя ID: {}", userId);

        User user = findUserById(userId);
        user.clearLocation();

        User savedUser = userRepository.save(user);
        log.info("✅ Геолокация пользователя {} очищена", userId);

        return userMapper.toResponseDto(savedUser);
    }

    // === PRIVATE UTILITY МЕТОДЫ ===

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));
    }

    private void updateUserAddressFields(User user, UpdateUserLocationDto locationDto) {
        user.setStreet(locationDto.getStreet());
        user.setCity(locationDto.getCity());
        user.setRegion(locationDto.getRegion());
        user.setCountry(locationDto.getCountry());
        user.setPostalCode(locationDto.getPostalCode());
        user.setFullAddress(user.getFormattedAddress());

        log.debug("✅ Обновлены поля адреса пользователя: {}", user.getFormattedAddress());
    }

    private void performReverseGeocoding(User user, BigDecimal longitude, BigDecimal latitude) {
        try {
//             Здесь будет интеграция с MapboxGeocodingService
//             String address = geocodingService.reverseGeocode(longitude, latitude);
//             user.setFullAddress(address);

            // Временная заглушка
            String address = String.format("Адрес по координатам %s, %s", latitude, longitude);
            user.setFullAddress(address);
            log.info("✅ Получен адрес через reverse geocoding: {}", address);

        } catch (Exception e) {
            log.warn("⚠️ Не удалось получить адрес через reverse geocoding: {}", e.getMessage());
        }
    }

    private void performForwardGeocoding(User user) {
        try {
            // Здесь будет интеграция с MapboxGeocodingService
            // GeoLocation coordinates = geocodingService.geocodeAddress(user.getFormattedAddress());
            // user.setLatitude(coordinates.getLatitude());
            // user.setLongitude(coordinates.getLongitude());
            // user.setLocationUpdatedAt(LocalDateTime.now());

            log.info("✅ Координаты получены через geocoding для адреса: {}", user.getFormattedAddress());

        } catch (Exception e) {
            log.warn("⚠️ Не удалось получить координаты через geocoding: {}", e.getMessage());
        }
    }

    private List<NearbyStoreDto> callStoreServiceForNearbyStores(FindNearbyStoresDto searchDto) {
        try {
            // 🌐 Используем Eureka для обращения к сервису магазинов
            String url = "http://" + serviceNames.getProductService() + "/api/stores/nearby";

            log.debug("🔗 Вызов сервиса магазинов: {}", url);

            // Отправляем POST запрос с параметрами поиска
            NearbyStoreDto[] response = restTemplate.postForObject(url, searchDto, NearbyStoreDto[].class);

            if (response != null) {
                List<NearbyStoreDto> stores = List.of(response);
                log.info("✅ Получено {} магазинов от сервиса {}", stores.size(), serviceNames.getProductService());
                return stores;
            }

            log.warn("⚠️ Пустой ответ от сервиса магазинов");
            return List.of();

        } catch (Exception e) {
            log.error("❌ Ошибка вызова сервиса магазинов через Eureka: {}", e.getMessage());

            // 🔄 Возвращаем временные тестовые данные для разработки
            log.info("🔄 Возвращаем тестовые данные для разработки");
            return createMockStores(searchDto);
        }
    }

    private StoreLocationDto getStoreLocation(Long storeId) {
        try {
            // 🌐 Используем Eureka для получения информации о магазине
            String url = "http://" + serviceNames.getProductService() + "/api/stores/" + storeId + "/location";

            log.debug("🔗 Получение локации магазина: {}", url);

            StoreLocationDto storeLocation = restTemplate.getForObject(url, StoreLocationDto.class);

            if (storeLocation != null) {
                log.info("✅ Получена локация магазина {} от сервиса {}", storeId, serviceNames.getProductService());
                return storeLocation;
            }

            throw new RuntimeException("Пустой ответ от сервиса магазинов");

        } catch (Exception e) {
            log.error("❌ Ошибка получения локации магазина {} через Eureka: {}", storeId, e.getMessage());

            // 🔄 Возвращаем тестовые данные для разработки
            log.info("🔄 Возвращаем тестовые данные для магазина {}", storeId);
            return createMockStoreLocation(storeId);
        }
    }

    // 🧪 Методы для тестовых данных (убрать в продакшене)
    private List<NearbyStoreDto> createMockStores(FindNearbyStoresDto searchDto) {
        return List.of(
                NearbyStoreDto.builder()
                        .storeId(1L)
                        .name("Тестовый магазин 1")
                        .description("Описание магазина")
                        .category("Фастфуд")
                        .latitude(searchDto.getLatitude().add(new BigDecimal("0.01")))
                        .longitude(searchDto.getLongitude().add(new BigDecimal("0.01")))
                        .distanceKm(1.2)
                        .distanceText("1.2 км")
                        .estimatedDeliveryTime(25)
                        .rating(4.5)
                        .isOpen(true)
                        .deliveryFee(new BigDecimal("100"))
                        .minOrderAmount(new BigDecimal("500"))
                        .build(),
                NearbyStoreDto.builder()
                        .storeId(2L)
                        .name("Тестовый магазин 2")
                        .description("Еще один магазин")
                        .category("Пицца")
                        .latitude(searchDto.getLatitude().add(new BigDecimal("0.02")))
                        .longitude(searchDto.getLongitude().add(new BigDecimal("0.02")))
                        .distanceKm(2.1)
                        .distanceText("2.1 км")
                        .estimatedDeliveryTime(35)
                        .rating(4.2)
                        .isOpen(true)
                        .deliveryFee(new BigDecimal("150"))
                        .minOrderAmount(new BigDecimal("700"))
                        .build()
        );
    }

    private StoreLocationDto  createMockStoreLocation(Long storeId) {
        return StoreLocationDto.builder()
                .storeId(storeId)
                .name("Тестовый магазин " + storeId)
                .latitude(new BigDecimal("50.4501"))
                .longitude(new BigDecimal("30.5234"))
                .address("Тестовый адрес магазина " + storeId)
                .build();
    }

    /**
     * 📏 Расчет расстояния между двумя точками по формуле Haversine
     */
    private double calculateDistanceInKm(BigDecimal lat1, BigDecimal lon1,
                                         BigDecimal lat2, BigDecimal lon2) {
        final double R = 6371; // Радиус Земли в км

        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lonDistance = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0f м", distanceKm * 1000);
        }
        return String.format("%.1f км", distanceKm);
    }

    private String formatCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    private String formatShortAddress(String city, String country) {
        if (city == null && country == null) {
            return null;
        }
        if (city != null && country != null) {
            return city + ", " + country;
        }
        return city != null ? city : country;
    }

    private int calculateDeliveryTime(double distanceKm) {
        // Используем настройки из конфигурации
        return (int) Math.round(
                (distanceKm / locationConfig.getDelivery().getSpeedKmh()) * 60 +
                        locationConfig.getDelivery().getBaseTimeMinutes()
        );
    }

    private BigDecimal calculateDeliveryFee(double distanceKm) {
        // Используем настройки из конфигурации
        BigDecimal baseFee = locationConfig.getDelivery().getBaseFee();
        BigDecimal perKmFee = locationConfig.getDelivery().getFeePerKm();
        BigDecimal distanceFee = perKmFee.multiply(BigDecimal.valueOf(distanceKm));

        return baseFee.add(distanceFee);
    }
}


