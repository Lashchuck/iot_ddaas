package com.iot_ddaas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Ta klasa obsługuje wszystkie wyjątki rzucane przez aplikację i zwraca odpowiednie odpowiedzi.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Obsługa wszystkich ogólnych wyjątków, które nie są specjalnie obsługiwane.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e){
        // Logowanie błędu
        logger.error("An error occured: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.");
    }

    // Obsługa wyjąteków NoHandlerFoundException, gdy adres URL nie pasuje do żadnej obsługi.
    // Sprawdza brakujące zasoby statyczne, takie jak CSS, JS lub favicon.
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNoHandlerFoundException(NoHandlerFoundException e){
        String requestUrl = e.getRequestURL();

        // Sprawdzanie, czy adres URL żądania pasuje do zasobów statycznych (CSS, JS, favicon).
        if (requestUrl.startsWith("/css/") || requestUrl.startsWith("/js/") || requestUrl.startsWith("/images/") || requestUrl.equals("/favicon.ico")) {
            logger.warn("Static resource not found: {}", requestUrl);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found.");
        }
        logger.error("No handler found for request: {}", requestUrl, e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.");
    }
}
