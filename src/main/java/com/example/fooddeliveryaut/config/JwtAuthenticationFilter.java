package com.example.fooddeliveryaut.config;

import com.example.fooddeliveryaut.service.AuthService;
import com.example.fooddeliveryaut.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Проверяем наличие токена
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Извлекаем токен
        jwt = authHeader.substring(7);

        try {
            // Извлекаем email из токена
            userEmail = jwtUtil.getEmailFromToken(jwt);

            // Если email есть и пользователь не аутентифицирован
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Загружаем пользователя
                UserDetails userDetails = authService.loadUserByUsername(userEmail);

                // Проверяем токен
                if (jwtUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Пользователь {} аутентифицирован", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка аутентификации: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}