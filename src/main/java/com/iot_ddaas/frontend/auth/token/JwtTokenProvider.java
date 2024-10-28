package com.iot_ddaas.frontend.auth.token;

import com.iot_ddaas.frontend.auth.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String secretKeyBase64;

    private SecretKey secretKey;

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final long EXPIRATION_TIME = 86400000; // 1 dzień w ms

    @PostConstruct
    public void init(){
        // Dekodowanie klucza Base64 na obiekt SecretKey
        log.info("SecretKeyBase64: {}", secretKeyBase64);
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyBase64));
        log.info("Initialized SecretKey: {}", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    public SecretKey getSecretKey(){
        return secretKey;
    }

    public String generateToken(UserDto userDto) {
        log.info("Generating token for user: {}", userDto.getEmail());
        Map<String, Object> claims = new HashMap<>();
        // Dodaj rolę użytkownika do claims
        claims.put("role", userDto.getRole());
        claims.put("email", userDto.getEmail());

        return createToken(claims, userDto.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public Optional<String> getEmailFromToken(String token) {
        if (token == null || token.split("\\.").length != 3) {
            log.error("Invalid token format: {}", token);
            return Optional.empty();
        }

        try {
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Dodanie getBytes() dla poprawnego klucza
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject()); // Pobranie emaila z tokenu
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Claims> getAllClaimsFromToken(String token) {
        try {
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody());
        } catch (JwtException | IllegalArgumentException  e) {
            log.error("Failed to parse claims from token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        System.out.println("Validating token:" + token);
        log.info("Validating token with SecretKey: {}", secretKey);
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            log.info("Token is valid.");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token verification failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        Optional<Claims> claimsOptional = getAllClaimsFromToken(token);
        if (claimsOptional.isPresent()) {
            Date expirationDate = claimsOptional.get().getExpiration();
            return expirationDate.before(new Date());
        }
        return true;
    }
}