package com.iot_ddaas.frontend.auth.token;

import com.iot_ddaas.frontend.auth.CustomUserDetails;
import com.iot_ddaas.frontend.auth.User;
import com.iot_ddaas.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Filtr  do uwierzytelniania żądań przy użyciu tokenów JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository){
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Pobranie nagłówka autoryzacji z żądania
        String authorizationHeader = request.getHeader("Authorization");

        // Obejście walidacji tokenów dla określonych endpointów
        if ("/iot/data".equals(request.getRequestURI()) ||
            "/auth/login".equals(request.getRequestURI()) ||
            "/auth/register".equals(request.getRequestURI()) ||
            "/favicon.ico".equals(request.getRequestURI()) ||
            "/".equals(request.getRequestURI()) ||
            "/index.html".equals(request.getRequestURI()) ||
            "/logo192.png".equals(request.getRequestURI()) ||
            "/actuator/health".equals(request.getRequestURI()) ||
            request.getRequestURI().startsWith("/css/") ||
            request.getRequestURI().startsWith("/js/") ||
            request.getRequestURI().startsWith("/static/")) {
            chain.doFilter(request, response); // Kontynuuj bez walidacji tokena
            return;
        }

        // Sprawdzenie, czy nagłówek autoryzacji jest obecny i zaczyna się od "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            logger.debug("Extracted token: {}", token);

            // Logowanie formatu tokena
            System.out.println("Authorization header: " + authorizationHeader);
            System.out.println("Token length: " + token.length());
            System.out.println("Token parts: " + token.split("\\.").length);

            // Sprawdzanie formatu tokena
            String[] parts = token.split("\\.");
            if (parts.length !=3){
                logger.error("Invalid token format. Token do not have 3 parts: {}", token);
                response.setContentType("text/plain");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid token format");
                return;
            }

            // Debugowanie części tokena
            logger.debug("Token header: {}", parts[0]);
            logger.debug("Token payload: {}", parts[1]);
            logger.debug("Token signature: {}", parts[2]);

            logger.debug("About to validate token: {}", token);
            try {
                logger.debug("Calling isTokenValid for token: {}", token);

                if (!jwtTokenProvider.isTokenValid(token)) {
                    logger.error("Invalid token format, problem with SecretKey: {}", token);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
                    return;
                }
            }catch (Exception e){
                logger.error("Error during token validation: {}", e.getMessage());
            }

            // Sprawdzanie, czy token wygasł
            if (jwtTokenProvider.isTokenExpired(token)){
                System.err.println("Token is expired: " + token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is expired");
                return;
            }

            // Wyodrębnienie adresu e-mail z tokena i uwierzytelnienie użytkownika
            try {
                Optional<String> optionalEmail = jwtTokenProvider.getEmailFromToken(token);

                if (optionalEmail.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String email = optionalEmail.get();
                    logger.debug("Attempting to find user by email: {}", email);
                    User user = userRepository.findByEmail(email);
                    System.out.println("User found: " + user);

                    if (user != null){
                        setAuthentication(user, request); // Ustaw uwierzytelnianie w kontekście zabezpieczeń
                    }else {
                        logger.error("User not found for email: {}", email);
                    }
                }
            } catch (Exception e) {
                logger.error("Token validation error: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return; // Zatrzymanie dalszego przetwarzania, jeśli token jest nieprawidłowy
            }
        }else {
            logger.error("Invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * Ustawienie uwierzytelniania w kontekście zabezpieczeń na podstawie danych użytkownika.
     */
    private void setAuthentication(User user, HttpServletRequest request){
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("Authentication successful for: {}", user.getUsername());
    }
}
