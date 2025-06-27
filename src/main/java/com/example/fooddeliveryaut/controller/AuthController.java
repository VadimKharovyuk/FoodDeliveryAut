package com.example.fooddeliveryaut.controller;

import com.example.fooddeliveryaut.dto.AuthResponseDto;
import com.example.fooddeliveryaut.dto.LoginRequestDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        log.info("Попытка авторизации пользователя: {}", loginRequest.getEmail());

        try {
            AuthResponseDto authResponse = authService.login(loginRequest);
            log.info("Пользователь {} успешно авторизован", loginRequest.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.warn("Неудачная попытка авторизации для email: {}", loginRequest.getEmail());
            throw new RuntimeException(e.getMessage());
        }
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        log.debug("Запрос информации о текущем пользователе");

        try {
            // Извлекаем токен из заголовка "Bearer TOKEN"
            String token = authHeader.substring(7);
            UserResponseDto user = authService.getCurrentUser(token);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            log.error("Ошибка получения информации о пользователе: {}", e.getMessage());
            throw new RuntimeException("Ошибка получения информации о пользователе");
        }
    }


    @PostMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UserResponseDto user = authService.getCurrentUser(token);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Пользователь вышел из системы");
        // TODO: Добавить токен в blacklist в будущем
        return ResponseEntity.ok("Успешный выход из системы");
    }


}