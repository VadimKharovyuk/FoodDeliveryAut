package com.example.fooddeliveryaut.service;

import com.example.fooddeliveryaut.config.MapboxConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapboxGeocodingService implements GeocodingService {

    private final MapboxConfigProperties mapboxConfig;
    private final RestTemplate restTemplate;

    // Флаг для определения доступности геокодирования
    private boolean geocodingAvailable = false;

    @PostConstruct
    public void initializeMapboxService() {
        log.info("🗺️ Initializing Mapbox Geocoding Service...");

        // Проверяем конфигурацию
        if (!mapboxConfig.hasValidToken()) {
            log.warn("⚠️ Mapbox access token is not configured or invalid!");
            log.warn("⚠️ Add 'MAPBOX_ACCESS_TOKEN=your_token_here' to your environment variables");
            log.warn("⚠️ Get your token at: https://account.mapbox.com/access-tokens/");
            log.warn("⚠️ Geocoding will use fallback coordinates");
            geocodingAvailable = false;
        } else {
            // Маскируем токен для безопасности в логах
            String maskedToken = maskToken(mapboxConfig.getAccess().getToken());
            log.info("🔑 Mapbox token loaded: {}", maskedToken);

            // Проверяем валидность токена
            try {
                validateToken();
                geocodingAvailable = true;
                log.info("✅ Mapbox Geocoding Service initialized successfully");
                log.info("🌍 Geocoding is ENABLED");
            } catch (Exception e) {
                log.error("❌ Mapbox token validation failed: {}", e.getMessage());
                log.warn("⚠️ Geocoding will use fallback coordinates");
                geocodingAvailable = false;
            }
        }

        // Выводим статус сервиса
        log.info("📊 Mapbox Service Status:");
        log.info("   • Token configured: {}", mapboxConfig.hasValidToken());
        log.info("   • Geocoding available: {}", geocodingAvailable);
        log.info("   • Fallback mode: {}", !geocodingAvailable);
        log.info("   • Country filter: {}", mapboxConfig.getGeocoding().getCountry());
        log.info("   • Results limit: {}", mapboxConfig.getGeocoding().getLimit());
    }

    /**
     * 📍 Реализация прямого геокодирования из интерфейса
     */
    @Override
    public GeoLocation geocodeAddress(String address) {
        if (!geocodingAvailable) {
            log.warn("⚠️ Geocoding service unavailable, using fallback for address: {}", address);
            return getFallbackCoordinatesForAddress(address);
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format("%s/%s.json?%s",
                    mapboxConfig.getGeocodingUrl(),
                    encodedAddress,
                    mapboxConfig.getDefaultParams());

            log.debug("🔍 Geocoding request URL: {}", url.replace(mapboxConfig.getAccess().getToken(), "***"));

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                MapboxFeature feature = response.getFeatures().get(0);
                double[] coordinates = feature.getGeometry().getCoordinates();

                // Mapbox возвращает [longitude, latitude]
                BigDecimal longitude = BigDecimal.valueOf(coordinates[0]).setScale(8, RoundingMode.HALF_UP);
                BigDecimal latitude = BigDecimal.valueOf(coordinates[1]).setScale(8, RoundingMode.HALF_UP);

                log.info("✅ Successfully geocoded '{}' to [{}, {}]", address, latitude, longitude);
                return new GeoLocation(latitude, longitude);
            } else {
                log.warn("⚠️ No results found for address: {}, using fallback", address);
                return getFallbackCoordinatesForAddress(address);
            }
        } catch (Exception e) {
            log.error("❌ Error during Mapbox geocoding for address: {}, using fallback", address, e);
            return getFallbackCoordinatesForAddress(address);
        }
    }

    /**
     * 🔄 Реализация обратного геокодирования из интерфейса
     */
    @Override
    public String reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        if (!geocodingAvailable) {
            log.warn("⚠️ Geocoding service unavailable for reverse geocoding");
            return String.format("Координаты: %s, %s", latitude, longitude);
        }

        try {
            String url = String.format("%s/%s,%s.json?access_token=%s&types=address&country=%s",
                    mapboxConfig.getGeocodingUrl(),
                    longitude,
                    latitude,
                    mapboxConfig.getAccess().getToken(),
                    mapboxConfig.getGeocoding().getCountry());

            log.debug("🔄 Reverse geocoding request for coordinates: [{}, {}]", latitude, longitude);

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                String address = response.getFeatures().get(0).getPlace_name();
                log.info("✅ Successfully reverse geocoded [{}, {}] to '{}'", latitude, longitude, address);
                return address;
            } else {
                log.warn("⚠️ No address found for coordinates [{}, {}]", latitude, longitude);
                return String.format("Координаты: %s, %s", latitude, longitude);
            }
        } catch (Exception e) {
            log.error("❌ Error during reverse geocoding for coordinates [{}, {}]", latitude, longitude, e);
            return String.format("Координаты: %s, %s", latitude, longitude);
        }
    }

    /**
     * 🔍 Реализация поиска ближайших мест из интерфейса
     */
    @Override
    public List<MapboxPlace> searchNearbyPlaces(BigDecimal longitude, BigDecimal latitude, String query, int limit) {
        if (!geocodingAvailable) {
            log.warn("⚠️ Geocoding service unavailable for nearby places search");
            return Collections.emptyList();
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("%s/%s.json?access_token=%s&proximity=%s,%s&limit=%d&types=poi&country=%s",
                    mapboxConfig.getGeocodingUrl(),
                    encodedQuery,
                    mapboxConfig.getAccess().getToken(),
                    longitude,
                    latitude,
                    Math.min(limit, mapboxConfig.getGeocoding().getLimit()),
                    mapboxConfig.getGeocoding().getCountry());

            log.debug("🔍 Searching nearby places for query: '{}' near [{}, {}]", query, latitude, longitude);

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null) {
                List<MapboxPlace> places = response.getFeatures().stream()
                        .map(this::convertFeatureToPlace)
                        .collect(Collectors.toList());

                log.info("✅ Found {} nearby places for query: '{}'", places.size(), query);
                return places;
            }

            log.warn("⚠️ No nearby places found for query: '{}'", query);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("❌ Error searching nearby places for query: '{}'", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * ✅ Реализация проверки доступности сервиса из интерфейса
     */
    @Override
    public boolean isGeocodingAvailable() {
        return geocodingAvailable;
    }

    /**
     * 🏠 Реализация создания адреса с координатами из интерфейса
     */
    @Override
    public Address createAddressWithCoordinates(CreateAddressRequest request) {
        log.info("🏠 Creating address with coordinates for: {}", request.getStreet());

        Address.AddressBuilder builder = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .region(request.getRegion())
                .country(request.getCountry())
                .postalCode(request.getPostalCode());

        // Если координаты переданы напрямую
        if (request.getLatitude() != null && request.getLongitude() != null) {
            builder.latitude(request.getLatitude())
                    .longitude(request.getLongitude());
            log.info("📍 Using provided coordinates: [{}, {}]", request.getLatitude(), request.getLongitude());
        }
        // Пробуем геокодирование если доступно
        else if (geocodingAvailable && (request.getAutoGeocode() == null || request.getAutoGeocode())) {
            try {
                String address = getFormattedAddress(request);
                log.info("🌍 Geocoding address: {}", address);

                GeoLocation coordinates = geocodeAddress(address);
                builder.latitude(coordinates.getLatitude())
                        .longitude(coordinates.getLongitude())
                        .fullAddress(address);
                log.info("✅ Geocoded '{}' → [{}, {}]",
                        address, coordinates.getLatitude(), coordinates.getLongitude());

            } catch (Exception e) {
                log.warn("⚠️ Geocoding failed: {}, using fallback", e.getMessage());
                BigDecimal[] fallbackCoords = getFallbackCoordinatesByCity(request.getCity(), request.getCountry());
                builder.latitude(fallbackCoords[0]).longitude(fallbackCoords[1]);
            }
        } else {
            // Используем fallback координаты
            log.info("🔄 Using fallback coordinates (geocoding unavailable or disabled)");
            BigDecimal[] fallbackCoords = getFallbackCoordinatesByCity(request.getCity(), request.getCountry());
            builder.latitude(fallbackCoords[0]).longitude(fallbackCoords[1]);
        }

        builder.fullAddress(getFormattedAddress(request));
        Address result = builder.build();

        log.info("✅ Address created with coordinates: [{}, {}]",
                result.getLatitude(), result.getLongitude());

        return result;
    }

    // === PRIVATE UTILITY МЕТОДЫ ===

    /**
     * Проверяет валидность токена делая тестовый запрос
     */
    private void validateToken() {
        try {
            log.debug("🔍 Validating Mapbox token...");

            String testAddress = "Moscow";
            String encodedAddress = URLEncoder.encode(testAddress, StandardCharsets.UTF_8);
            String url = String.format("%s/%s.json?%s",
                    mapboxConfig.getGeocodingUrl(),
                    encodedAddress,
                    mapboxConfig.getDefaultParams());

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null) {
                log.info("✅ Mapbox token is valid - test geocoding successful");
            } else {
                throw new RuntimeException("Invalid response from Mapbox API");
            }

        } catch (Exception e) {
            throw new RuntimeException("Token validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Маскирует токен для безопасного логирования
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***invalid***";
        }

        return token.substring(0, 8) + "..." + token.substring(token.length() - 4)
                + " (length: " + token.length() + ")";
    }

    /**
     * Преобразует MapboxFeature в MapboxPlace
     */
    private MapboxPlace convertFeatureToPlace(MapboxFeature feature) {
        double[] coordinates = feature.getGeometry().getCoordinates();
        return MapboxPlace.builder()
                .name(feature.getText())
                .fullName(feature.getPlace_name())
                .longitude(BigDecimal.valueOf(coordinates[0]))
                .latitude(BigDecimal.valueOf(coordinates[1]))
                .build();
    }

    /**
     * Получает fallback координаты для адреса
     */
    private GeoLocation getFallbackCoordinatesForAddress(String address) {
        // Простая логика определения города из адреса
        String lowerAddress = address.toLowerCase();

        if (lowerAddress.contains("харьков") || lowerAddress.contains("kharkiv")) {
            return new GeoLocation(new BigDecimal("49.9935"), new BigDecimal("36.2304"));
        }
        if (lowerAddress.contains("киев") || lowerAddress.contains("kiev") || lowerAddress.contains("kyiv")) {
            return new GeoLocation(new BigDecimal("50.4501"), new BigDecimal("30.5234"));
        }
        if (lowerAddress.contains("одесса") || lowerAddress.contains("odesa")) {
            return new GeoLocation(new BigDecimal("46.4825"), new BigDecimal("30.7233"));
        }
        if (lowerAddress.contains("москва") || lowerAddress.contains("moscow")) {
            return new GeoLocation(new BigDecimal("55.7558"), new BigDecimal("37.6176"));
        }
        if (lowerAddress.contains("петербург") || lowerAddress.contains("spb")) {
            return new GeoLocation(new BigDecimal("59.9311"), new BigDecimal("30.3609"));
        }

        // По умолчанию - центр Европы
        log.debug("🌍 Using default European coordinates for unknown address: {}", address);
        return new GeoLocation(new BigDecimal("50.0000"), new BigDecimal("20.0000"));
    }

    /**
     * Получает примерные координаты по названию города
     */
    private BigDecimal[] getFallbackCoordinatesByCity(String city, String country) {
        if (city == null) city = "";
        if (country == null) country = "";

        String key = (city + ", " + country).toLowerCase();

        // Украина
        if (key.contains("харьков") || key.contains("kharkiv") || key.contains("kharkov")) {
            return new BigDecimal[]{new BigDecimal("49.9935"), new BigDecimal("36.2304")};
        }
        if (key.contains("киев") || key.contains("kiev") || key.contains("kyiv")) {
            return new BigDecimal[]{new BigDecimal("50.4501"), new BigDecimal("30.5234")};
        }
        if (key.contains("одесса") || key.contains("odesa") || key.contains("odessa")) {
            return new BigDecimal[]{new BigDecimal("46.4825"), new BigDecimal("30.7233")};
        }

        // Россия
        if (key.contains("москва") || key.contains("moscow")) {
            return new BigDecimal[]{new BigDecimal("55.7558"), new BigDecimal("37.6176")};
        }
        if (key.contains("петербург") || key.contains("spb") || key.contains("petersburg")) {
            return new BigDecimal[]{new BigDecimal("59.9311"), new BigDecimal("30.3609")};
        }

        // США
        if (key.contains("new york")) {
            return new BigDecimal[]{new BigDecimal("40.7128"), new BigDecimal("-74.0060")};
        }
        if (key.contains("los angeles")) {
            return new BigDecimal[]{new BigDecimal("34.0522"), new BigDecimal("-118.2437")};
        }

        // Германия
        if (key.contains("berlin")) {
            return new BigDecimal[]{new BigDecimal("52.5200"), new BigDecimal("13.4050")};
        }

        // По умолчанию - центр Европы
        log.debug("🌍 Using default European coordinates for unknown city: {}", city);
        return new BigDecimal[]{new BigDecimal("50.0000"), new BigDecimal("20.0000")};
    }

    /**
     * Форматирует адрес из запроса
     */
    private String getFormattedAddress(CreateAddressRequest request) {
        return String.format("%s, %s%s%s",
                request.getStreet(),
                request.getCity(),
                request.getRegion() != null ? ", " + request.getRegion() : "",
                request.getCountry() != null ? ", " + request.getCountry() : ""
        );
    }
}

// === DTO КЛАССЫ ДЛЯ MAPBOX API ===

/**
 * Ответ от Mapbox Geocoding API
 */
class MapboxGeocodingResponse {
    private String type;
    private String[] query;
    private List<MapboxFeature> features;
    private String attribution;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String[] getQuery() { return query; }
    public void setQuery(String[] query) { this.query = query; }

    public List<MapboxFeature> getFeatures() { return features; }
    public void setFeatures(List<MapboxFeature> features) { this.features = features; }

    public String getAttribution() { return attribution; }
    public void setAttribution(String attribution) { this.attribution = attribution; }
}

/**
 * Feature из ответа Mapbox
 */
class MapboxFeature {
    private String id;
    private String type;
    private String place_name;
    private String[] place_type;
    private double relevance;
    private String text;
    private MapboxGeometry geometry;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPlace_name() { return place_name; }
    public void setPlace_name(String place_name) { this.place_name = place_name; }

    public String[] getPlace_type() { return place_type; }
    public void setPlace_type(String[] place_type) { this.place_type = place_type; }

    public double getRelevance() { return relevance; }
    public void setRelevance(double relevance) { this.relevance = relevance; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public MapboxGeometry getGeometry() { return geometry; }
    public void setGeometry(MapboxGeometry geometry) { this.geometry = geometry; }
}

/**
 * Геометрия из ответа Mapbox
 */
class MapboxGeometry {
    private String type;
    private double[] coordinates;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double[] getCoordinates() { return coordinates; }
    public void setCoordinates(double[] coordinates) { this.coordinates = coordinates; }
}