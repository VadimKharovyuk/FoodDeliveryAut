package com.example.fooddeliveryaut.dto;

import com.example.fooddeliveryaut.enums.UserRole;

public interface UserProjection {
    Long getId();
    String getEmail();
    String getFirstName();
    String getLastName();
    UserRole getUserRole();
}
