package com.example.fooddeliveryaut.service.impl;

import com.example.fooddeliveryaut.dto.AuthResponseDto;
import com.example.fooddeliveryaut.dto.LoginRequestDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.mapper.UserMapper;
import com.example.fooddeliveryaut.model.User;
import com.example.fooddeliveryaut.service.AuthService;
import com.example.fooddeliveryaut.service.UserService;
import com.example.fooddeliveryaut.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        log.info("Попытка авторизации пользователя: {}", loginRequest.getEmail());

        // Находим пользователя
        User user = userService.findByEmail(loginRequest.getEmail());

        // Проверяем пароль
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный email или пароль");
        }

        // Генерируем JWT токен
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getUserRole().getAuthority()
        );

        // Создаем ответ
        UserResponseDto userDto = userMapper.toResponseDto(user);

        log.info("Пользователь {} успешно авторизован", user.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCredentials(String email, String password) {
        try {
            User user = userService.findByEmail(email);
            return passwordEncoder.matches(password, user.getPassword());
        } catch (Exception e) {
            log.warn("Ошибка валидации учетных данных для email: {}", email);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Невалидный токен");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userService.findByEmail(email);

        return userMapper.toResponseDto(user);
    }

    /**
     * 🔐 Реализация UserDetailsService для Spring Security
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Загрузка пользователя по email: {}", email);

        try {
            User user = userService.findByEmail(email);

            // Создаем authorities на основе роли
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(user.getUserRole().getAuthority())
            );

            // Возвращаем Spring Security User
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false) // TODO: добавить поле isActive в будущем
                    .build();

        } catch (Exception e) {
            log.error("Пользователь не найден: {}", email);
            throw new UsernameNotFoundException("Пользователь с email " + email + " не найден");
        }
    }
}