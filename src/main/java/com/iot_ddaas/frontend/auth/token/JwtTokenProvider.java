package com.iot_ddaas.frontend.auth.token;

import com.iot_ddaas.frontend.auth.UserDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "secret";
    private final long EXPIRATION_TIME = 86400000; // 1 dzień w ms

    public String generateToken(UserDto user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public String getEmailFromToken(String token) {

        if (token == null || token.split("\\.").length != 3){
            System.out.println("Invalid token format: " + token);
            return null;
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes()) // Dodanie getBytes() dla poprawnego klucza
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // Pobranie emaila z tokenu
        } catch (Exception e) {
            System.out.println("Invalid token: " + e.getMessage());
            System.out.println("Token parsing error: " + e.getMessage());
            throw  new RuntimeException("Invalid token format: " + token);
        }
    }

    public boolean isTokenValid(String token){
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
