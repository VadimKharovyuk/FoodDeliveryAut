package com.example.fooddeliveryaut.controller;



import com.example.fooddeliveryaut.dto.UserRegistrationDto;
import com.example.fooddeliveryaut.dto.UserResponseDto;
import com.example.fooddeliveryaut.enums.UserRole;
import com.example.fooddeliveryaut.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 📝 Контроллер для регистрации пользователей
 */
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegistrationDto registrationDto) {
        log.info("Запрос на регистрацию пользователя с email: {}", registrationDto.getEmail());

        try {
            UserResponseDto user = userService.registerUser(registrationDto);
            log.info("Пользователь {} успешно зарегистрирован", user.getEmail());
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            log.error("Ошибка регистрации пользователя: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<UserRole>> getAvailableRoles() {
        List<UserRole> roles = Arrays.asList(UserRole.getRegistrationRoles());
        log.debug("Запрос доступных ролей для регистрации");
        return ResponseEntity.ok(roles);
    }




    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = !userService.existsByEmail(email);
        return ResponseEntity.ok(isAvailable);
    }


}
