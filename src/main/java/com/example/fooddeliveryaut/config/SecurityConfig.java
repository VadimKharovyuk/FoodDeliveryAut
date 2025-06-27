package com.example.fooddeliveryaut.config;

import com.example.fooddeliveryaut.config.security.JwtAuthenticationEntryPoint;
import com.example.fooddeliveryaut.config.security.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DataSource dataSource;

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
                        .requestMatchers("/actuator/**").permitAll()

                        // 🔒 Защищенные endpoints
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/validate-token").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()

                        // 👑 Endpoints для ролей
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/business/**").hasAnyRole("BUSINESS", "ADMIN")
                        .requestMatchers("/api/courier/**").hasAnyRole("COURIER", "ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "BUSINESS", "COURIER", "ADMIN")

                        .anyRequest().permitAll()
                )
                // 🍪 Remember Me конфигурация
                .rememberMe(remember -> remember
                        .key("food-delivery-remember-me-key") // Секретный ключ
                        .tokenRepository(persistentTokenRepository()) // БД для токенов
                        .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 дней
                        .userDetailsService(null) // Укажем в AuthService
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 🍪 Repository для хранения Remember Me токенов в БД
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}