//package com.example.fooddeliveryaut.mapper;
//
//import com.example.fooddeliveryaut.dto.UserResponseDto;
//import com.example.fooddeliveryaut.model.User;
//import org.springframework.stereotype.Component;
//@Component
//public class UserMapper {
//
//
//    public UserResponseDto toResponseDto(User user) {
//        if (user == null) {
//            return null;
//        }
//
//        return UserResponseDto.builder()
//                .id(user.getId())
//                .email(user.getEmail())
//                .firstName(user.getFirstName())
//                .lastName(user.getLastName())
//                .userRole(user.getUserRole())
//                .roleDisplayName(user.getUserRole() != null ?
//                        user.getUserRole().getDisplayName() : null)
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
//                .build();
//    }
//}

package com.example.fooddeliveryaut.mapper;

import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.enums.UserRole;
import com.example.fooddeliveryaut.model.User;
import org.springframework.stereotype.Component;

/**
 * 🔄 Маппер для конвертации User entity в DTO и обратно
 * Обновлен для поддержки геолокации
 */
@Component
public class UserMapper {

    /**
     * 📤 Конвертация User entity в UserResponseDto
     */
    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                // === ОСНОВНАЯ ИНФОРМАЦИЯ ===
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userRole(user.getUserRole())
                .roleDisplayName(getRoleDisplayName(user.getUserRole()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())

                // === ГЕОЛОКАЦИЯ ===
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .street(user.getStreet())
                .city(user.getCity())
                .region(user.getRegion())
                .country(user.getCountry())
                .postalCode(user.getPostalCode())
                .fullAddress(user.getFullAddress())
                .locationUpdatedAt(user.getLocationUpdatedAt())

                // === COMPUTED FIELDS ===
                .hasLocation(user.hasLocation())
                .formattedCoordinates(formatCoordinates(user.getLatitude(), user.getLongitude()))
                .shortAddress(formatShortAddress(user.getCity(), user.getCountry()))
                .locationStatus(getLocationStatus(user))

                .build();
    }

    /**
     * 📥 Конвертация UserResponseDto в User entity (для обновлений)
     * ВНИМАНИЕ: Используйте осторожно, так как может перезаписать данные
     */
    public void updateUserFromDto(User user, UserResponseDto dto) {
        if (user == null || dto == null) {
            return;
        }

        // Обновляем только безопасные поля
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        // Геолокация (только если передана)
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            user.setLatitude(dto.getLatitude());
            user.setLongitude(dto.getLongitude());
        }

        if (dto.getStreet() != null) {
            user.setStreet(dto.getStreet());
        }
        if (dto.getCity() != null) {
            user.setCity(dto.getCity());
        }
        if (dto.getRegion() != null) {
            user.setRegion(dto.getRegion());
        }
        if (dto.getCountry() != null) {
            user.setCountry(dto.getCountry());
        }
        if (dto.getPostalCode() != null) {
            user.setPostalCode(dto.getPostalCode());
        }
        if (dto.getFullAddress() != null) {
            user.setFullAddress(dto.getFullAddress());
        }
    }

    // === UTILITY МЕТОДЫ ===

    /**
     * 🎭 Получение отображаемого имени роли
     */
    private String getRoleDisplayName(UserRole userRole) {
        if (userRole == null) {
            return "Неизвестная роль";
        }

        return switch (userRole) {
            case ADMIN -> "Администратор";
            case BASE_USER -> "Пользователь";
            case BUSINESS_USER -> "Владелец магазина";
            case COURIER -> "Курьер"; // если есть такая роль
        };
    }

    /**
     * 📍 Форматирование координат для отображения
     */
    private String formatCoordinates(java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * 🏠 Форматирование краткого адреса
     */
    private String formatShortAddress(String city, String country) {
        if (city == null && country == null) {
            return null;
        }
        if (city != null && country != null) {
            return city + ", " + country;
        }
        return city != null ? city : country;
    }

    /**
     * 📊 Определение статуса геолокации
     */
    private String getLocationStatus(User user) {
        if (!user.hasLocation()) {
            return "Не установлена";
        }

        if (user.getLocationUpdatedAt() == null) {
            return "Установлена";
        }

        // Проверяем, не устарела ли геолокация (старше 30 дней)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (user.getLocationUpdatedAt().isBefore(now.minusDays(30))) {
            return "Устарела";
        }

        // Проверяем, обновлялась ли на этой неделе
        if (user.getLocationUpdatedAt().isAfter(now.minusWeeks(1))) {
            return "Актуальна";
        }

        return "Установлена";
    }

    /**
     * 📋 Создание краткого DTO только с основной информацией (без геолокации)
     */
    public UserResponseDto toBasicResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userRole(user.getUserRole())
                .roleDisplayName(getRoleDisplayName(user.getUserRole()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .hasLocation(user.hasLocation())
                .locationStatus(getLocationStatus(user))
                .build();
    }

    /**
     * 📍 Создание DTO только с геолокацией (для специализированных endpoints)
     */
    public UserResponseDto toLocationOnlyDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
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
                .locationStatus(getLocationStatus(user))
                .build();
    }
}