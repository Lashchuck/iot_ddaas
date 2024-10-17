package com.iot_ddaas.frontend.auth;

import com.github.dockerjava.api.model.AuthResponse;
import com.iot_ddaas.frontend.auth.token.JwtResponse;
import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// Kontroler odpowiedzialny za rejestrację i logowanie użytkowników
@RestController
// Mapowanie żądań związanych z autoryzacją do /auth
@RequestMapping("/auth")
public class AuthController {

    // Wstrzyknięcie serwisu użytkownika
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto){
        String encryptedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encryptedPassword);
        // Wywołanie serwisu w celu zarejestrowania użytkownika
        userService.registerUser(userDto);
        return ResponseEntity.ok("Użytkownik zarejestrowany");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest){
        Map<String, Object> response = new HashMap<>();
        try {
            // Wywołanie serwisu w celu zalogowania użytkownika
            UserDto userDto = userService.loginUser(loginRequest);

            // Generowanie tokenu JWT
            String token = jwtTokenProvider.generateToken(userDto);
            System.out.println("Generated token: " + token);

            // Sprawdzanie czy token został poprawnie wygenerowany
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Błąd podczas generowania tokenu"));
            }

            response.put("token", token);
            response.put("user", Map.of("id", userDto.getId(), "email", userDto.getEmail()));

            return ResponseEntity.ok(response);

        }catch (UserNotFoundException ex){
            response.put("message", "Nieprawidłowy email lub hasło");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }catch (Exception ex){
            // Obsługa innych wyjątków
            response.put("message", "Wystąpił błąd podczas logowania");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Obsługa wyjątku UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex){
        // Zwracanie odpowiedzi HTTP 404 z komunikatem o błędzie
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
