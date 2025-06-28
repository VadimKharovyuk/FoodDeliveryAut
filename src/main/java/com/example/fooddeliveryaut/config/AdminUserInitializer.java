// AdminUserInitializer.java
package com.example.fooddeliveryaut.config;

import com.example.fooddeliveryaut.enums.UserRole;
import com.example.fooddeliveryaut.model.User;
import com.example.fooddeliveryaut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@fooddelivery.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.create-default:true}")
    private boolean createDefaultAdmin;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!createDefaultAdmin) {
            log.info("🚫 Default admin creation disabled");
            return;
        }

        // Проверяем, есть ли уже ADMIN пользователи
        boolean adminExists = userRepository.existsByUserRole(UserRole.ADMIN);

        if (adminExists) {
            log.info("👑 Admin user already exists, skipping creation");
            return;
        }

        // Проверяем, не существует ли пользователь с таким email
        if (userRepository.existsByEmail(adminEmail)) {
            log.warn("⚠️ User with email {} already exists, updating role to ADMIN", adminEmail);

            User existingUser = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            existingUser.setUserRole(UserRole.ADMIN); // Исправлено на userRole
            userRepository.save(existingUser);

            log.info("✅ Updated existing user {} to ADMIN role", adminEmail);
            return;
        }

        // Создаем нового ADMIN пользователя
        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setFirstName("Super");
        adminUser.setLastName("Admin");
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setUserRole(UserRole.ADMIN);


        User savedAdmin = userRepository.save(adminUser);

        log.info("👑 Default ADMIN user created successfully:");
        log.info("   📧 Email: {}", savedAdmin.getEmail());
        log.info("   🔑 Password: {} (change this!)", adminPassword);
        log.info("   🆔 ID: {}", savedAdmin.getId());
        log.info("   🛡️ Role: {}", savedAdmin.getUserRole());
    }
}
