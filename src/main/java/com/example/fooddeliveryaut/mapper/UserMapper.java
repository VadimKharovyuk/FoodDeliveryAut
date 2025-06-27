package com.example.fooddeliveryaut.mapper;

import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.model.User;
import org.springframework.stereotype.Component;


import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .imgUrl(user.getImgUrl())
                .userRole(user.getUserRole())
                .roleDisplayName(user.getUserRole() != null ?
                        user.getUserRole().getDisplayName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}