package com.example.fooddeliveryaut.service;


import com.example.fooddeliveryaut.dto.AuthResponseDto;
import com.example.fooddeliveryaut.dto.LoginRequestDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {

    /**
     * 🔐 Авторизация пользователя
     */
    AuthResponseDto login(LoginRequestDto loginRequest);

    /**
     * ✅ Проверка учетных данных
     */
    boolean validateCredentials(String email, String password);

    /**
     * 🎯 Получить текущего пользователя по токену
     */
    UserResponseDto getCurrentUser(String token);
}
