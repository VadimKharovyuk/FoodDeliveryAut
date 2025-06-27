package com.example.fooddeliveryaut.dto;

import com.example.fooddeliveryaut.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {


    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String imgUrl;

    private UserRole userRole;

    private String roleDisplayName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}