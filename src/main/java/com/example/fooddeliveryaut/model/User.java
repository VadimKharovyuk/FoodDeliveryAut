package com.example.fooddeliveryaut.model;

import com.example.fooddeliveryaut.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;


//    / 🌍 === ГЕОЛОКАЦИЯ ПОЛЬЗОВАТЕЛЯ === 🌍

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    // 📍 Адрес пользователя
    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "region")
    private String region;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    // 📍 Полный адрес (кэшированный)
    @Column(name = "full_address", length = 500)
    private String fullAddress;

    // 🕐 Время последнего обновления координат
    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;



    // 🔧 === UTILITY МЕТОДЫ === 🔧

    /**
     * Проверяет, есть ли у пользователя геолокация
     */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    /**
     * Получает отформатированный адрес
     */
    public String getFormattedAddress() {
        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
            return fullAddress;
        }

        StringBuilder sb = new StringBuilder();
        if (street != null && !street.trim().isEmpty()) {
            sb.append(street);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (region != null && !region.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(region);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }

        return sb.toString();
    }

    /**
     * Устанавливает координаты и обновляет время
     */
    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationUpdatedAt = LocalDateTime.now();
    }

    /**
     * Очищает геолокацию пользователя
     */
    public void clearLocation() {
        this.latitude = null;
        this.longitude = null;
        this.locationUpdatedAt = null;
    }

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}
