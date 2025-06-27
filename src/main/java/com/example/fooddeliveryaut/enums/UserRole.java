package com.example.fooddeliveryaut.enums;

import lombok.Getter;


@Getter
public enum UserRole {


    BASE_USER("Покупатель", "USER"),

    BUSINESS_USER("Владелец магазина", "BUSINESS"),


    COURIER("Курьер", "COURIER"),


    ADMIN("Администратор", "ADMIN");


    private final String displayName;

    /**
     * 🔐 Техническое название для Spring Security authorities
     */
    private final String authority;

    UserRole(String displayName, String authority) {
        this.displayName = displayName;
        this.authority = "ROLE_" + authority; // Spring Security конвенция
    }


    public static UserRole[] getRegistrationRoles() {
        return new UserRole[]{BASE_USER, BUSINESS_USER, COURIER};
    }

    /**
     * ✅ Проверить, может ли роль быть назначена при регистрации
     */
    public boolean isRegistrationAllowed() {
        return this != ADMIN;
    }

    /**
     * 🏪 Проверить, является ли роль бизнес-ролью
     */
    public boolean isBusinessRole() {
        return this == BUSINESS_USER;
    }

    /**
     * 🚚 Проверить, является ли роль курьерской
     */
    public boolean isCourierRole() {
        return this == COURIER;
    }
}