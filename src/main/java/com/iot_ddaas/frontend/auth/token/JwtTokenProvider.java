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

/**
 * Ta klasa zapewnia narzędzia do generowania, analizowania i walidacji tokenów JWT.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String secretKeyBase64;

    private SecretKey secretKey;

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Czas wygaśnięcia tokena ustawiony na 1 dzień (w milisekundach)
    private final long EXPIRATION_TIME = 86400000;

    /**
     * Inicjalizacja obiektu SecretKey poprzez dekodowanie klucza (secretKey) zakodowanego w Base64.
     */
    @PostConstruct
    public void init(){
        // Dekodowanie klucza Base64 na obiekt SecretKey
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyBase64));
    }

    /**
     * Zwraca zainicjowany SecretKey.
     */
    public SecretKey getSecretKey(){
        return secretKey;
    }

    /**
     * Generuje token JWT dla danego użytkownika.
     */
    public String generateToken(UserDto userDto) {
        log.info("Generating token for user: {}", userDto.getEmail());

        // Dodaj rolę użytkownika do claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDto.getRole());
        claims.put("email", userDto.getEmail());

        return createToken(claims, userDto.getEmail());
    }

    /**
     * Tworzy token JWT z określonymi żadaniami i tematem.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Wyodrębnia wiadomość e-mail z podanego tokena JWT.
     */
    public Optional<String> getEmailFromToken(String token) {
        if (token == null || token.split("\\.").length != 3) {
            log.error("Invalid token format: {}", token);
            return Optional.empty();
        }

        try {
            return Optional.of(Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Weryfikuje token przy użyciu secretKey
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject()); // Pobranie emaila z tokenu
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Wyodrębnia wszystkie żądania z podanego tokena JWT.
     */
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

    /**
     * Weryfikuje dostarczony token JWT.
     */
    public boolean isTokenValid(String token) {
        log.info("Validating token with SecretKey: {}", secretKey);

        try {
            // Parsowanie tokenu, aby upewnić się, że jest prawidłowy.
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            log.info("Token is valid.");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Sprawdza, czy podany token JWT wygasł.
     */
    public boolean isTokenExpired(String token) {
        Optional<Claims> claimsOptional = getAllClaimsFromToken(token);

        if (claimsOptional.isPresent()) {
            Date expirationDate = claimsOptional.get().getExpiration(); // Pobieranie daty wygaśnięcia z żądań
            return expirationDate.before(new Date()); // Porównanie z aktualnym czasem
        }
        return true; // Jeśli roszczenia nie są obecne, token jest nieważny.
    }
}