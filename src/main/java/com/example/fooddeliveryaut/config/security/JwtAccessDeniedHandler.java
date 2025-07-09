package com.example.fooddeliveryaut.config.security;
import com.example.fooddeliveryaut.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;


@Component
@Slf4j
public class JwtAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {

        log.error("🚫 Доступ запрещен к {}: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // Создаем ApiResponse для ошибки доступа
        ApiResponse<Object> apiResponse = ApiResponse.<Object>builder()
                .success(false)
                .message("Недостаточно прав для выполнения операции")
                .errorCode("ACCESS_DENIED")
                .errorDetails(accessDeniedException.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        // Настраиваем response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Записываем JSON ответ
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}