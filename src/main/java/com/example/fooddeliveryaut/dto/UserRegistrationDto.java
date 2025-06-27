package com.example.fooddeliveryaut.dto;



import com.example.fooddeliveryaut.enums.UserRole;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private UserRole userRole;
}
