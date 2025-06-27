package com.example.fooddeliveryaut.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Публичные endpoints
                        .requestMatchers("/api/registration/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/test").permitAll()
                        .requestMatchers("/api/registration/test").permitAll()
                        .requestMatchers("/actuator/**").permitAll()  // Actuator endpoints

                        // 🔒 Защищенные endpoints
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/validate-token").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()

                        // 👑 Endpoints для админа (в будущем)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 🏪 Endpoints для бизнес-пользователей (в будущем)
                        .requestMatchers("/api/business/**").hasAnyRole("BUSINESS", "ADMIN")

                        // 🚚 Endpoints для курьеров (в будущем)
                        .requestMatchers("/api/courier/**").hasAnyRole("COURIER", "ADMIN")

                        // 🛒 Endpoints для пользователей (в будущем)
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "BUSINESS", "COURIER", "ADMIN")

                        // Все остальное пока открыто (для разработки)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}