package com.example.fooddeliveryaut.service.impl;

import com.example.fooddeliveryaut.dto.UserRegistrationDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.dto.UserProjection;
import com.example.fooddeliveryaut.mapper.UserMapper;
import com.example.fooddeliveryaut.model.User;
import com.example.fooddeliveryaut.repository.UserRepository;
import com.example.fooddeliveryaut.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        log.info("Регистрация пользователя с email: {}", registrationDto.getEmail());

        // Проверяем, существует ли пользователь
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Создаем пользователя
        User user = User.builder()
                .email(registrationDto.getEmail())
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .userRole(registrationDto.getUserRole())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: {}", savedUser.getId());

        // Возвращаем DTO вместо Entity
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    // 🚀 Новые методы с DTO/Projection

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findUserDtoByEmail(String email) {
        User user = findByEmail(email);
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProjection findUserProjectionByEmail(String email) {
        return userRepository.findProjectionByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProjection> getAllUsersProjections() {
        return userRepository.findAllProjections();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    @Override
    public User findById(Long userId) {
        return null;
    }

    @Override
    public UserResponseDto findUserDtoById(Long userId) {
        User user = findById(userId);
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserProjection findUserProjectionById(Long userId) {
        return userRepository.findProjectionById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + userId + " не найден"));
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }
}