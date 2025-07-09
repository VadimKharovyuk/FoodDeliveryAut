package com.example.fooddeliveryaut.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration; /**
 * 🔗 Конфигурация для интеграции с другими сервисами
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.services")
public class ServiceNamesProperties {

    /**
     * Имя сервиса продуктов/магазинов в Eureka
     */
    private String productService = "PRODUCT-SERVICE";

    /**
     * Имя frontend сервиса в Eureka
     */
    private String frontendService = "FRONTEND-SERVICE";
}
