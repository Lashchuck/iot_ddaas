package com.iot_ddaas.frontend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Klasa konfiguracyjna do ustawiania CORS i obsługi zasobów dla frontendu.
 * Obsługuje mapowanie zasobów statycznych (CSS, JS itp.) i umożliwia żądania z różnych źródeł.
 */
@Configuration
@EnableWebMvc // Włączenie konfiguracji Spring Web MVC
public class WebConfig implements WebMvcConfigurer {

    /**
     * Konfiguracja ustawień Cross-Origin Resource Sharing (CORS).
     * Pozwala to frontendowi (localhost:3000) na dostęp do backendu (localhost:8080).
     */
    @Override
    public void addCorsMappings(CorsRegistry registry){
        // Zezwalenie na żądania cross-origin z określonych źródeł.
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedOrigins("http://192.168.1.3")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * Konfiguracja obsługi zasobów do serwowania zasobów statycznych, takich jak CSS i JS.
     * Zasoby są obsługiwane z katalogu /static.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/logo192.png")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/static/");
    }
}
