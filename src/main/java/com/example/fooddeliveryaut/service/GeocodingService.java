package com.example.fooddeliveryaut.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 🌍 Интерфейс для сервиса геокодирования
 *
 * Предоставляет методы для:
 * - Преобразования адресов в координаты (geocoding)
 * - Преобразования координат в адреса (reverse geocoding)
 * - Поиска мест по запросу
 */
public interface GeocodingService {

    /**
     * 📍 Прямое геокодирование - преобразование адреса в координаты
     *
     * @param address строка адреса для геокодирования
     * @return объект с координатами (широта, долгота)
     * @throws RuntimeException если адрес не найден или сервис недоступен
     */
    GeoLocation geocodeAddress(String address);

    /**
     * 🔄 Обратное геокодирование - преобразование координат в адрес
     *
     * @param longitude долгота
     * @param latitude широта
     * @return строка с адресом
     * @throws RuntimeException если адрес не найден или сервис недоступен
     */
    String reverseGeocode(BigDecimal longitude, BigDecimal latitude);

    /**
     * 🔍 Поиск ближайших мест по запросу
     *
     * @param longitude долгота центральной точки
     * @param latitude широта центральной точки
     * @param query поисковый запрос
     * @param limit максимальное количество результатов
     * @return список найденных мест
     */
    List<MapboxPlace> searchNearbyPlaces(BigDecimal longitude, BigDecimal latitude, String query, int limit);

    /**
     * ✅ Проверка доступности сервиса геокодирования
     *
     * @return true если сервис доступен, false если используется fallback режим
     */
    boolean isGeocodingAvailable();

    /**
     * 🏠 Создание адреса с автоматическим геокодированием
     *
     * @param request запрос на создание адреса
     * @return объект адреса с координатами
     */
    Address createAddressWithCoordinates(CreateAddressRequest request);
}

/**
 * 📍 Класс для хранения географических координат
 */
class GeoLocation {
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public GeoLocation(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return String.format("GeoLocation{latitude=%s, longitude=%s}", latitude, longitude);
    }
}

/**
 * 🏪 Класс для описания найденного места
 */
class MapboxPlace {
    private String name;
    private String fullName;
    private BigDecimal longitude;
    private BigDecimal latitude;

    public MapboxPlace() {}

    public static MapboxPlaceBuilder builder() {
        return new MapboxPlaceBuilder();
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public static class MapboxPlaceBuilder {
        private String name;
        private String fullName;
        private BigDecimal longitude;
        private BigDecimal latitude;

        public MapboxPlaceBuilder name(String name) { this.name = name; return this; }
        public MapboxPlaceBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public MapboxPlaceBuilder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public MapboxPlaceBuilder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }

        public MapboxPlace build() {
            MapboxPlace place = new MapboxPlace();
            place.name = this.name;
            place.fullName = this.fullName;
            place.longitude = this.longitude;
            place.latitude = this.latitude;
            return place;
        }
    }
}

/**
 * 🏠 Класс для создания адреса
 */
class Address {
    private String street;
    private String city;
    private String region;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String fullAddress;

    public Address() {}

    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public static class AddressBuilder {
        private String street;
        private String city;
        private String region;
        private String country;
        private String postalCode;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String fullAddress;

        public AddressBuilder street(String street) { this.street = street; return this; }
        public AddressBuilder city(String city) { this.city = city; return this; }
        public AddressBuilder region(String region) { this.region = region; return this; }
        public AddressBuilder country(String country) { this.country = country; return this; }
        public AddressBuilder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public AddressBuilder latitude(BigDecimal latitude) { this.latitude = latitude; return this; }
        public AddressBuilder longitude(BigDecimal longitude) { this.longitude = longitude; return this; }
        public AddressBuilder fullAddress(String fullAddress) { this.fullAddress = fullAddress; return this; }

        public Address build() {
            Address address = new Address();
            address.street = this.street;
            address.city = this.city;
            address.region = this.region;
            address.country = this.country;
            address.postalCode = this.postalCode;
            address.latitude = this.latitude;
            address.longitude = this.longitude;
            address.fullAddress = this.fullAddress;
            return address;
        }
    }
}

/**
 * 📝 Класс запроса для создания адреса
 */
class CreateAddressRequest {
    private String street;
    private String city;
    private String region;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean autoGeocode;

    public CreateAddressRequest() {}

    // Getters and Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public Boolean getAutoGeocode() { return autoGeocode; }
    public void setAutoGeocode(Boolean autoGeocode) { this.autoGeocode = autoGeocode; }
}