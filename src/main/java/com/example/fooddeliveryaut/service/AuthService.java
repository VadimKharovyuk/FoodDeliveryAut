package com.example.fooddeliveryaut.service;


import com.example.fooddeliveryaut.dto.AuthResponseDto;
import com.example.fooddeliveryaut.dto.LoginRequestDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {


    AuthResponseDto login(LoginRequestDto loginRequest);


    boolean validateCredentials(String email, String password);


    UserResponseDto getCurrentUser(String token);

    AuthResponseDto loginWithRememberMe(LoginRequestDto loginRequest, boolean rememberMe);
}
