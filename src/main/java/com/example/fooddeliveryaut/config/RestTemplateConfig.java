package com.example.fooddeliveryaut.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 🌐 Конфигурация для интеграции с микросервисами через Eureka
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 🔗 RestTemplate с поддержкой Load Balancing через Eureka
     *
     * @LoadBalanced аннотация позволяет использовать имена сервисов вместо URL
     * Например: http://PRODUCT-SERVICE/api/stores/nearby
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 📞 Обычный RestTemplate без Load Balancing (для внешних API)
     * Можно использовать для вызовов к внешним сервисам (Mapbox, Google Maps, etc.)
     */
    @Bean("plainRestTemplate")
    public RestTemplate plainRestTemplate() {
        return new RestTemplate();
    }
}