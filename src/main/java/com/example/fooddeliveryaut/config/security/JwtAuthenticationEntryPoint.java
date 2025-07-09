package com.example.fooddeliveryaut.config.security;

import com.example.fooddeliveryaut.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 🔐 Обработчик неавторизованных запросов (401 Unauthorized)
 * Возвращает стандартный ApiResponse для ошибок аутентификации
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Для поддержки LocalDateTime
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("🔒 Неавторизованный доступ к {}: {}", request.getRequestURI(), authException.getMessage());

        // Определяем тип ошибки и сообщение
        String errorMessage;
        String errorCode;

        String exceptionMessage = authException.getMessage().toLowerCase();

        if (exceptionMessage.contains("jwt") || exceptionMessage.contains("token")) {
            errorMessage = "Недействительный или истекший токен";
            errorCode = "INVALID_TOKEN";
        } else if (exceptionMessage.contains("expired")) {
            errorMessage = "Токен истек";
            errorCode = "TOKEN_EXPIRED";
        } else if (exceptionMessage.contains("malformed")) {
            errorMessage = "Некорректный формат токена";
            errorCode = "MALFORMED_TOKEN";
        } else if (exceptionMessage.contains("signature")) {
            errorMessage = "Неверная подпись токена";
            errorCode = "INVALID_SIGNATURE";
        } else {
            errorMessage = "Требуется авторизация";
            errorCode = "UNAUTHORIZED";
        }

        // Создаем стандартный ApiResponse для ошибки
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .success(false)
                .message(errorMessage)
                .errorCode(errorCode)
                .errorDetails(authException.getMessage())
                .path(request.getRequestURI())
                .build();

        // Настраиваем response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Записываем JSON ответ
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);

        log.debug("🔒 Отправлен ответ об ошибке авторизации: {}", jsonResponse);
    }
}