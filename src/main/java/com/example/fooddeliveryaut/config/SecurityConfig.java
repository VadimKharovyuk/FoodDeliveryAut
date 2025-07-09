//package com.example.fooddeliveryaut.config;
//
//import com.example.fooddeliveryaut.config.security.JwtAuthenticationEntryPoint;
//import com.example.fooddeliveryaut.config.security.JwtAuthenticationFilter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
//import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//                .authorizeHttpRequests(auth -> auth
//                        // 🔓 Публичные endpoints
//                        .requestMatchers("/api/registration/**").permitAll()
//                        .requestMatchers("/api/auth/login").permitAll()
//                        .requestMatchers("/api/auth/test").permitAll()
//                        .requestMatchers("/api/registration/test").permitAll()
//                        .requestMatchers("/actuator/**").permitAll()
//
//                        // 🔒 Защищенные endpoints
//                        .requestMatchers("/api/auth/me").authenticated()
//                        .requestMatchers("/api/auth/validate-token").authenticated()
//                        .requestMatchers("/api/auth/logout").authenticated()
//
//                        // 👑 Endpoints для ролей
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/business/**").hasAnyRole("BUSINESS", "ADMIN")
//                        .requestMatchers("/api/courier/**").hasAnyRole("COURIER", "ADMIN")
//                        .requestMatchers("/api/user/**").hasAnyRole("USER", "BUSINESS", "COURIER", "ADMIN")
//
//                        .anyRequest().permitAll()
//                )
//
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}
package com.example.fooddeliveryaut.config;

import com.example.fooddeliveryaut.config.security.JwtAccessDeniedHandler;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler; // 🆕 Добавляем AccessDeniedHandler
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Публичные endpoints
                        .requestMatchers("/api/registration/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/test").permitAll()
                        .requestMatchers("/api/registration/test").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // 📱 Swagger и документация
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 🌐 Static ресурсы (ваш HTML тестер)
                        .requestMatchers("/location-api-tester.html", "/static/**").permitAll()

                        // 🔒 Защищенные auth endpoints
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/validate-token").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()

                        // 📍 === ГЕОЛОКАЦИЯ ПОЛЬЗОВАТЕЛЕЙ === 📍
                        .requestMatchers("/api/users/me/location").authenticated()              // GET/PUT/DELETE location
                        .requestMatchers("/api/users/me/location/status").authenticated()      // Status
                        .requestMatchers("/api/users/me/address").authenticated()              // PUT address
                        .requestMatchers("/api/users/me/nearby-stores").authenticated()        // GET/POST stores
                        .requestMatchers("/api/users/me/distance-to-store/**").authenticated() // Distance calc

                        // 📊 Статистика геолокации - только для админов
                        .requestMatchers("/api/users/location-stats").hasRole("ADMIN")

                        // 👑 Endpoints для ролей (ваши существующие)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/business/**").hasAnyRole("BUSINESS_USER", "ADMIN")
                        .requestMatchers("/api/courier/**").hasAnyRole("COURIER", "ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("BASE_USER", "BUSINESS_USER", "COURIER", "ADMIN")

                        // 👤 Общие пользовательские endpoints - требуют аутентификации
                        .requestMatchers("/api/users/me/**").authenticated()

                        // 🔓 Все остальное пока разрешено (можете изменить на authenticated())
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401
                        .accessDeniedHandler(jwtAccessDeniedHandler)           // 🆕 403
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}