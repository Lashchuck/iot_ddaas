package com.iot_ddaas.frontend.auth.token;

import com.iot_ddaas.frontend.auth.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final long EXPIRATION_TIME = 86400000; // 1 dzień w ms

    public String generateToken(UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDto.getEmail());
        System.out.println("Generated token: " + token);
        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {

        if (token == null || token.split("\\.").length != 3){
            System.out.println("Invalid token format: " + token);
            return null;
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Dodanie getBytes() dla poprawnego klucza
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // Pobranie emaila z tokenu
        } catch (Exception e) {
            System.out.println("Invalid token: " + e.getMessage());
            System.err.println("Token parsing error: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token){
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean isTokenExpired(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey) // użyj swojego klucza do podpisywania
                .parseClaimsJws(token)
                .getBody();
        Date expirationDate = claims.getExpiration();
        return expirationDate.before(new Date());
    }
}
