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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository){
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        logger.info("JwtTokenProvider injected: {}", jwtTokenProvider != null);
        logger.info("UserRepository injected: {}", userRepository !=null);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        logger.info("JwtAuthenticationFilter is called.");
        logger.info("Request URI: {}", request.getRequestURI());

        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Received Authorization header: " + authorizationHeader);

        if ("/iot/data".equals(request.getRequestURI())) {
            // Kontynuuj bez walidacji tokena
            chain.doFilter(request, response);
            return;
        }

        if ("/auth/login".equals(request.getRequestURI())){
            // Kontynuacja bez walidacji tokena
            chain.doFilter(request, response);
            return;
        }

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

            if (jwtTokenProvider.isTokenExpired(token)){
                System.err.println("Token is expired: " + token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is expired");
                return;
            }

            try {
                Optional<String> optionalEmail = jwtTokenProvider.getEmailFromToken(token);

                if (optionalEmail.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String email = optionalEmail.get();
                    logger.debug("Attempting to find user by email: {}", email);
                    User user = userRepository.findByEmail(email);
                    System.out.println("User found: " + user);

                    if (user != null){
                        setAuthentication(user, request);
                    }else {
                        logger.error("User not found for email: {}", email);
                    }
                }
            } catch (Exception e) {
                logger.error("Token validation error: {}", e.getMessage());
                // Logowanie błędu (możesz użyć loggera zamiast System.out)
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
