package com.iot_ddaas.frontend.auth.token;

import com.iot_ddaas.frontend.auth.User;
import com.iot_ddaas.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Received Authorization header: " + authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            System.out.println("Extracted token: " + token);

            // Logowanie formatu tokena
            System.out.println("Authorization header: " + authorizationHeader);
            System.out.println("Token length: " + token.length());
            System.out.println("Token parts: " + token.split("\\.").length);

            // Sprawdzanie formatu tokena
            if (token.split("\\.").length !=3){
                System.err.println("Invalid token format: " + token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
                return;
            }

            if (jwtTokenProvider.isTokenExpired(token)){
                System.err.println("Token is expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is expired");
                return;
            }

            try {
                String email = jwtTokenProvider.getEmailFromToken(token);
                System.out.println("Exctracted email from token: " + email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmail(email);

                    if (user !=null){
                        System.out.println("Setting authentication for user: " + email);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                user, null, null);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                }else {
                    System.err.println("User not found: " + email);
                }
                }else {
                    System.err.println("Authentication failed for user: " + email);
                }
            } catch (Exception e) {
                System.err.println("Token validation error: " + e.getMessage());
                // Logowanie błędu (możesz użyć loggera zamiast System.out)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return; // Zatrzymanie dalszego przetwarzania, jeśli token jest nieprawidłowy
            }
        }else {
            System.err.println("Invalid Authorization  header");
        }
        chain.doFilter(request, response);
    }
}
