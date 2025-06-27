package com.example.fooddeliveryaut.dto;



import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
    private Boolean rememberMe;
}
