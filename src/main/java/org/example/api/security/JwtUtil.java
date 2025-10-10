package org.example.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    @Value("${jwt.secret}")
    private static String SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hora

    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Gera o token JWT com o email do usuário
    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida o token e retorna o email do usuário
    public static String validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
