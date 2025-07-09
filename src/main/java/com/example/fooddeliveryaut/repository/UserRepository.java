package com.example.fooddeliveryaut.repository;

import com.example.fooddeliveryaut.dto.CityStatsDto;
import com.example.fooddeliveryaut.dto.CountryStatsDto;
import com.example.fooddeliveryaut.dto.UserProjection;
import com.example.fooddeliveryaut.enums.UserRole;
import com.example.fooddeliveryaut.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 📧 Основные методы поиска
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // 🎯 Projection методы с @Query
    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u WHERE u.email = :email")
    Optional<UserProjection> findProjectionByEmail(@Param("email") String email);

    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u")
    List<UserProjection> findAllProjections();

    @Query("SELECT u.id as id, u.email as email, u.firstName as firstName, " +
            "u.lastName as lastName, u.userRole as userRole " +
            "FROM User u WHERE u.id = :id")
    Optional<UserProjection> findProjectionById(@Param("id") Long id);

    // 🔧 Методы для работы с ролями
    boolean existsByUserRole(UserRole userRole);

    // 🌍 === ГЕОЛОКАЦИЯ МЕТОДЫ (ИСПРАВЛЕННЫЕ ДЛЯ POSTGRESQL) === 🌍

    /**
     * 📊 Подсчет пользователей, обновивших геолокацию сегодня
     * Исправлено для PostgreSQL
     */
    @Query("SELECT COUNT(u) FROM User u WHERE CAST(u.locationUpdatedAt AS date) = CURRENT_DATE")
    long countUsersWithLocationUpdatedToday();

    /**
     * 📊 Альтернативный метод через native SQL для большей совместимости
     */
    @Query(value = "SELECT COUNT(*) FROM users u WHERE DATE(u.location_updated_at) = CURRENT_DATE",
            nativeQuery = true)
    long countUsersWithLocationUpdatedTodayNative();

    /**
     * 📊 Подсчет пользователей, обновивших геолокацию на этой неделе
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.locationUpdatedAt >= :weekStart")
    long countUsersWithLocationUpdatedThisWeek(@Param("weekStart") LocalDateTime weekStart);

    /**
     * 📊 Подсчет пользователей, обновивших геолокацию в этом месяце
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.locationUpdatedAt >= :monthStart")
    long countUsersWithLocationUpdatedThisMonth(@Param("monthStart") LocalDateTime monthStart);

    /**
     * 📊 Подсчет пользователей с геолокацией
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.latitude IS NOT NULL AND u.longitude IS NOT NULL")
    long countUsersWithLocation();

    /**
     * 📊 Подсчет пользователей без геолокации
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.latitude IS NULL OR u.longitude IS NULL")
    long countUsersWithoutLocation();

    /**
     * 📊 Альтернативный метод для подсчета с геолокацией
     */
    long countByLatitudeIsNotNullAndLongitudeIsNotNull();

    /**
     * 📊 Статистика по городам (все города) - ИСПРАВЛЕНО
     */
    @Query("""
        SELECT new com.example.fooddeliveryaut.dto.CityStatsDto(
            u.city, 
            u.country, 
            COUNT(u), 
            (COUNT(u) * 100.0 / (SELECT COUNT(total) FROM User total))
        )
        FROM User u 
        WHERE u.city IS NOT NULL 
        GROUP BY u.city, u.country 
        ORDER BY COUNT(u) DESC
        """)
    List<CityStatsDto> getCityStatistics();

    /**
     * 📊 Статистика по странам
     */
    @Query("""
        SELECT new com.example.fooddeliveryaut.dto.CountryStatsDto(
            u.country, 
            COUNT(u), 
            (COUNT(u) * 100.0 / (SELECT COUNT(total) FROM User total))
        )
        FROM User u 
        WHERE u.country IS NOT NULL 
        GROUP BY u.country 
        ORDER BY COUNT(u) DESC
        """)
    List<CountryStatsDto> getCountryStatistics();

    /**
     * 📊 Топ N городов через native SQL
     */
    @Query(value = """
        SELECT u.city, 
               u.country, 
               COUNT(*) as user_count,
               (COUNT(*) * 100.0 / (SELECT COUNT(*) FROM users)) as percentage
        FROM users u 
        WHERE u.city IS NOT NULL 
        GROUP BY u.city, u.country 
        ORDER BY user_count DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopCitiesNative(@Param("limit") int limit);

    // === ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ===

    /**
     * Поиск пользователей в определенном радиусе от точки
     * Использует формулу Haversine для расчета расстояния
     */
    @Query(value = """
        SELECT u.*, 
               (6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * 
                           cos(radians(u.longitude) - radians(:longitude)) + 
                           sin(radians(:latitude)) * sin(radians(u.latitude)))) AS distance
        FROM users u 
        WHERE u.latitude IS NOT NULL 
          AND u.longitude IS NOT NULL
        HAVING distance <= :radiusKm 
        ORDER BY distance
        """, nativeQuery = true)
    List<User> findUsersWithinRadius(@Param("latitude") BigDecimal latitude,
                                     @Param("longitude") BigDecimal longitude,
                                     @Param("radiusKm") Double radiusKm);

    /**
     * 📊 Пользователи, обновившие геолокацию после определенной даты
     */
    @Query("SELECT u FROM User u WHERE u.locationUpdatedAt >= :fromDate")
    List<User> findUsersWithLocationUpdatedAfter(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 📊 Подсчет пользователей, обновивших геолокацию с определенной даты
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.locationUpdatedAt >= :fromDate")
    long countUsersWithLocationUpdatedSince(@Param("fromDate") LocalDateTime fromDate);

    /**
     * 📊 Поиск пользователей без геолокации
     */
    @Query("SELECT u FROM User u WHERE u.latitude IS NULL OR u.longitude IS NULL")
    List<User> findUsersWithoutLocation();

    /**
     * 📊 Поиск пользователей с устаревшей геолокацией
     */
    @Query("SELECT u FROM User u WHERE u.locationUpdatedAt < :cutoffDate OR u.locationUpdatedAt IS NULL")
    List<User> findUsersWithOutdatedLocation(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Поиск пользователей по городу
     */
    List<User> findByCityIgnoreCase(String city);

    /**
     * Поиск пользователей по городу и стране
     */
    List<User> findByCityIgnoreCaseAndCountryIgnoreCase(String city, String country);

    /**
     * Пользователи в определенном регионе/стране
     */
    List<User> findByCountryIgnoreCase(String country);
    List<User> findByRegionIgnoreCase(String region);

    /**
     * 📊 Поиск пользователей с геолокацией в определенных границах (bounding box)
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.latitude BETWEEN :minLat AND :maxLat 
          AND u.longitude BETWEEN :minLng AND :maxLng
        """)
    List<User> findUsersInBoundingBox(@Param("minLat") BigDecimal minLatitude,
                                      @Param("maxLat") BigDecimal maxLatitude,
                                      @Param("minLng") BigDecimal minLongitude,
                                      @Param("maxLng") BigDecimal maxLongitude);

    /**
     * 📊 Проверка, есть ли у пользователя геолокация
     */
    @Query("SELECT CASE WHEN u.latitude IS NOT NULL AND u.longitude IS NOT NULL THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean hasUserLocation(@Param("userId") Long userId);

    /**
     * 📊 Расширенная статистика через native SQL для лучшей производительности
     */
    @Query(value = """
        SELECT 
            CASE 
                WHEN location_updated_at >= CURRENT_DATE THEN 'Сегодня'
                WHEN location_updated_at >= CURRENT_DATE - INTERVAL '7 days' THEN 'На этой неделе'
                WHEN location_updated_at >= CURRENT_DATE - INTERVAL '30 days' THEN 'В этом месяце'
                WHEN location_updated_at IS NOT NULL THEN 'Старше месяца'
                ELSE 'Никогда'
            END as period,
            COUNT(*) as count
        FROM users 
        GROUP BY 
            CASE 
                WHEN location_updated_at >= CURRENT_DATE THEN 'Сегодня'
                WHEN location_updated_at >= CURRENT_DATE - INTERVAL '7 days' THEN 'На этой неделе'
                WHEN location_updated_at >= CURRENT_DATE - INTERVAL '30 days' THEN 'В этом месяце'
                WHEN location_updated_at IS NOT NULL THEN 'Старше месяца'
                ELSE 'Никогда'
            END
        ORDER BY 
            CASE 
                WHEN period = 'Сегодня' THEN 1
                WHEN period = 'На этой неделе' THEN 2
                WHEN period = 'В этом месяце' THEN 3
                WHEN period = 'Старше месяца' THEN 4
                ELSE 5
            END
        """, nativeQuery = true)
    List<Object[]> getLocationUpdateDistribution();

    /**
     * 📊 Среднее расстояние между пользователями в одном городе
     */
    @Query(value = """
        SELECT u1.city,
               AVG(
                   6371 * acos(
                       cos(radians(u1.latitude)) * cos(radians(u2.latitude)) * 
                       cos(radians(u2.longitude) - radians(u1.longitude)) + 
                       sin(radians(u1.latitude)) * sin(radians(u2.latitude))
                   )
               ) as avg_distance_km
        FROM users u1
        JOIN users u2 ON u1.city = u2.city AND u1.id != u2.id
        WHERE u1.latitude IS NOT NULL 
          AND u1.longitude IS NOT NULL
          AND u2.latitude IS NOT NULL 
          AND u2.longitude IS NOT NULL
          AND u1.city IS NOT NULL
        GROUP BY u1.city
        HAVING COUNT(*) > 1
        ORDER BY avg_distance_km DESC
        """, nativeQuery = true)
    List<Object[]> getAverageDistanceByCity();
}