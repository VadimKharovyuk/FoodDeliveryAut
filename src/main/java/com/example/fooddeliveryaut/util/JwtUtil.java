package com.example.fooddeliveryaut.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey1234567890123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 часа в миллисекундах
    private long jwtExpiration;

    @Value("${jwt.expiration.remember-me:604800000}") // 7 дней в миллисекундах
    private long jwtRememberMeExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 🔐 Генерирует JWT токен для пользователя
     */
    public String generateToken(String email, Long userId, String role) {
        return generateTokenWithExpiration(email, userId, role, jwtExpiration);
    }

    /**
     * 🍪 Генерирует JWT токен с Remember Me (7 дней)
     */
    public String generateRememberMeToken(String email, Long userId, String role) {
        return generateTokenWithExpiration(email, userId, role, jwtRememberMeExpiration);
    }

    /**
     * 🔐 Генерирует JWT токен с кастомным временем жизни
     */
    public String generateTokenWithExpiration(String email, Long userId, String role, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Остальные методы остаются без изменений...
    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Неверный JWT токен: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT токен не поддерживается: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims пустой: {}", e.getMessage());
        }
        return false;
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}