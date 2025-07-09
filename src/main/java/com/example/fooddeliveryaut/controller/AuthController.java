package com.example.fooddeliveryaut.controller;
import com.example.fooddeliveryaut.dto.ApiResponse;
import com.example.fooddeliveryaut.dto.AuthResponseDto;
import com.example.fooddeliveryaut.dto.LoginRequestDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.service.AuthService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto loginRequest) throws AuthenticationException {
        log.info("🔗 [INTER-SERVICE] Авторизация пользователя: {}", loginRequest.getEmail());

        try {
            AuthResponseDto authResponse = authService.login(loginRequest);

            log.info("✅ [INTER-SERVICE] Пользователь {} успешно авторизован (ID: {})",
                    loginRequest.getEmail(), authResponse.getUser().getId());

            return authResponse;

        } catch (BadCredentialsException e) {
            log.warn("❌ [INTER-SERVICE] Неверные credentials для email: {} - {}",
                    loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Неверный email или пароль");

        } catch (UsernameNotFoundException e) {
            log.warn("❌ [INTER-SERVICE] Пользователь не найден: {} - {}",
                    loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Неверный email или пароль");

        } catch (DisabledException e) {
            log.warn("❌ [INTER-SERVICE] Аккаунт отключен: {} - {}",
                    loginRequest.getEmail(), e.getMessage());
            throw new DisabledException("Аккаунт отключен");

        } catch (LockedException e) {
            log.warn("❌ [INTER-SERVICE] Аккаунт заблокирован: {} - {}",
                    loginRequest.getEmail(), e.getMessage());
            throw new LockedException("Аккаунт заблокирован");

        } catch (Exception e) {
            log.error("❌ [INTER-SERVICE] Неожиданная ошибка авторизации для email: {} - {}",
                    loginRequest.getEmail(), e.getMessage(), e);
            throw new AuthenticationException("Внутренняя ошибка авторизации");
        }
    }

    /**
     * 🔗 Получение пользователя по токену - ДЛЯ МЕЖСЕРВИСНЫХ ВЫЗОВОВ
     * Используется другими микросервисами для валидации токенов
     */
    @GetMapping("/user-by-token")
    public ResponseEntity<UserResponseDto> getUserByToken(
            @RequestHeader("Authorization") String authHeader) {

        log.debug("🔗 [INTER-SERVICE] Запрос пользователя по токену");

        try {
            String token = authHeader.substring(7);
            UserResponseDto user = authService.getCurrentUser(token);

            log.debug("✅ [INTER-SERVICE] Пользователь найден: {}", user.getEmail());

            // ✅ ПРЯМОЙ DTO для других сервисов
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            log.error("❌ [INTER-SERVICE] Ошибка получения пользователя: {}", e.getMessage());
            // ❌ Для межсервисных вызовов - HTTP статус без тела
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 🔗 Простая проверка валидности токена - ДЛЯ МЕЖСЕРВИСНЫХ ВЫЗОВОВ
     * Возвращает только true/false без дополнительных данных
     */
    @PostMapping("/validate-token-simple")
    public ResponseEntity<Boolean> validateTokenSimple(
            @RequestHeader("Authorization") String authHeader) {

        log.debug("🔗 [INTER-SERVICE] Простая проверка токена");

        try {
            String token = authHeader.substring(7);
            authService.getCurrentUser(token); // Если не бросит исключение - токен валиден

            log.debug("✅ [INTER-SERVICE] Токен валиден");
            return ResponseEntity.ok(true);

        } catch (Exception e) {
            log.debug("❌ [INTER-SERVICE] Токен невалиден: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // ========== КЛИЕНТСКИЕ ENDPOINTS (для фронтенда) ==========

    /**
     * 👤 Получение информации о текущем пользователе - ДЛЯ КЛИЕНТОВ
     * Возвращает обернутый ApiResponse для удобства фронтенда
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        log.debug("👤 [CLIENT] Запрос информации о текущем пользователе");

        try {
            String token = authHeader.substring(7);
            UserResponseDto user = authService.getCurrentUser(token);

            return ResponseEntity.ok(
                    ApiResponse.success(user, "Информация о пользователе получена")
            );

        } catch (Exception e) {
            log.error("❌ [CLIENT] Ошибка получения информации о пользователе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Ошибка получения информации о пользователе", "USER_INFO_ERROR"));
        }
    }

    /**
     * ✅ Проверка валидности токена - ДЛЯ КЛИЕНТОВ
     * Возвращает детальную информацию о токене
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ApiResponse<TokenValidationDto>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        log.debug("✅ [CLIENT] Проверка токена");

        try {
            String token = authHeader.substring(7);
            UserResponseDto user = authService.getCurrentUser(token);

            TokenValidationDto validation = TokenValidationDto.builder()
                    .valid(true)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getUserRole().toString())
                    .hasLocation(user.getHasLocation())
                    .locationStatus(user.getLocationStatus())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success(validation, "Токен действителен")
            );

        } catch (Exception e) {
            TokenValidationDto validation = TokenValidationDto.builder()
                    .valid(false)
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success(validation, "Токен недействителен")
            );
        }
    }

    /**
     * 🚪 Выход из системы - ДЛЯ КЛИЕНТОВ
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponseDto>> logout(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = authHeader.substring(7);
            UserResponseDto user = authService.getCurrentUser(token);

            log.info("🚪 [CLIENT] Пользователь {} вышел из системы", user.getEmail());

            // TODO: Добавить токен в blacklist в будущем
            // blacklistService.addToBlacklist(token);

            LogoutResponseDto logoutResponse = LogoutResponseDto.builder()
                    .success(true)
                    .userEmail(user.getEmail())
                    .logoutTime(java.time.LocalDateTime.now())
                    .message("Успешный выход из системы")
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success(logoutResponse, "Пользователь вышел из системы")
            );

        } catch (Exception e) {
            log.warn("❌ [CLIENT] Ошибка при выходе: {}", e.getMessage());

            LogoutResponseDto logoutResponse = LogoutResponseDto.builder()
                    .success(true) // Всё равно считаем успешным
                    .message("Выход из системы выполнен")
                    .logoutTime(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.success(logoutResponse, "Выход из системы выполнен")
            );
        }
    }

    /**
     * 🧪 Тестовый endpoint - ДЛЯ КЛИЕНТОВ
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<ServiceHealthDto>> test() {
        ServiceHealthDto health = ServiceHealthDto.builder()
                .serviceName("Auth Service")
                .status("UP")
                .message("Сервис авторизации работает!")
                .version("1.0.0")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(health, "Сервис доступен")
        );
    }

    // ========== DTO КЛАССЫ ==========

    /**
     * DTO для детальной валидации токена
     */
    @Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TokenValidationDto {
        private Boolean valid;
        private Long userId;
        private String email;
        private String role;
        private Boolean hasLocation;
        private String locationStatus;
    }

    /**
     * DTO для ответа logout
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LogoutResponseDto {
        private Boolean success;
        private String userEmail;
        private String message;
        private java.time.LocalDateTime logoutTime;
    }

    /**
     * DTO для health check
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ServiceHealthDto {
        private String serviceName;
        private String status;
        private String message;
        private String version;
        private java.time.LocalDateTime timestamp;
    }

    // ========== ОБРАБОТЧИКИ ОШИБОК ==========

    /**
     * ❌ Обработчик исключений - ТОЛЬКО ДЛЯ КЛИЕНТСКИХ ENDPOINTS
     * Межсервисные endpoints обрабатывают ошибки напрямую
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        log.error("❌ Ошибка в AuthController: {}", e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "GENERAL_ERROR";

        if (e.getMessage().contains("Invalid credentials") || e.getMessage().contains("Неверный")) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "INVALID_CREDENTIALS";
        } else if (e.getMessage().contains("токен") || e.getMessage().contains("token")) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "TOKEN_ERROR";
        } else if (e.getMessage().contains("не найден") || e.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            errorCode = "USER_NOT_FOUND";
        }

        return ResponseEntity.status(status)
                .body(ApiResponse.error(e.getMessage(), errorCode));
    }

    /**
     * ❌ Обработчик ошибок валидации
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Ошибка валидации: " + errorMessage, "VALIDATION_ERROR"));
    }
}