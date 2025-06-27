package com.example.fooddeliveryaut.service;

import com.example.fooddeliveryaut.dto.UserRegistrationDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.dto.UserProjection;
import com.example.fooddeliveryaut.model.User;

import java.util.List;

public interface UserService {

    // Основные методы
    UserResponseDto registerUser(UserRegistrationDto registrationDto);
    User findByEmail(String email);

    UserResponseDto findUserDtoByEmail(String email);

    UserProjection findUserProjectionByEmail(String email);
    List<UserProjection> getAllUsersProjections();


    boolean existsByEmail(String email);


    User findById(Long userId);
    UserResponseDto findUserDtoById(Long userId);
    UserProjection findUserProjectionById(Long userId);
    boolean existsById(Long userId);
}