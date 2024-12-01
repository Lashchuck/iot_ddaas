package com.iot_ddaas.frontend;

import com.iot_ddaas.frontend.auth.token.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Konfiguracja zabezpieczeń aplikacji, definiowanie reguł dostępu i filtrów zabezpieczeń.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Konstruktor, który wstrzykuje filtr JwtAuthenticationFilter.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Konfiguruje SecurityFilterChain, który definiuje reguły bezpieczeństwa dla żądań HTTP.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws  Exception{
        http
                // Wyłączenie ochrony XSS i opcje ramki
                .headers(headers -> headers
                        .xssProtection(xss -> xss.disable())
                        .frameOptions(frame -> frame.disable())
                )
                // Wyłączenie ochrony CSRF (nie jest potrzebna dla API z JWT)
                .csrf(csrf -> csrf.disable())
                // Włączenie CORS z niestandardową konfiguracją
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Definiowanie reguł autoryzacji dla żądań HTTP
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(  "/", "/favicon.ico",  "/auth/**", "/login", "/register",
                                "/index.html", "/static/**", "/css/**", "/js/**", "/images/**", "/logo192.png").permitAll() // Zezwolenie na dostęp endpointów
                        .requestMatchers("/iot/data", "/actuator/**").permitAll() // Zezwalanie na nieuwierzytelniony dostęp do danych IoT
                        .requestMatchers(HttpMethod.DELETE, "/iot/anomalies/**").authenticated() // Wymóg uwierzytelnienia dla usunięcia anomalii
                        .anyRequest().authenticated()) // Logowanie dla reszty zasobów
                        // Skonfiguracja zarządzania sesjami, aby używać sesji tylko wtedy, gdy jest to wymagane.
                        .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                        // Dodanie filtra uwierzytelniania JWT przed filtrem UsernamePasswordAuthenticationFilter.
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Konfiguruja ustawienia CORS (Cross-Origin Resource Sharing).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Zezwolenie na żądania z frontendu React działającego na localhost:3000
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // Zezwolenie na metody HTTP dla operacji CRUD
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Zezwolenie na określone nagłówki w żądaniach
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        // Zastosowanie konfigurację CORS do wszystkich punktów końcowych
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
