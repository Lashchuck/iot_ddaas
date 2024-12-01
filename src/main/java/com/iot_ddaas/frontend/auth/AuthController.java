package com.iot_ddaas.frontend.auth;

import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.repository.UserRepository;
import com.iot_ddaas.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    // Wstrzyknięcie serwisu użytkownika
    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto){
        // Wywołanie serwisu w celu zarejestrowania użytkownika
        userService.registerUser(userDto);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest){

        log.info("Login details received - email: {}, password: {}",  loginRequest.getEmail(), loginRequest.getPassword());

        User user = userRepository.findByEmail(loginRequest.getEmail());
        Map<String, Object> response = new HashMap<>();
        if (user == null){
            response.put("message", "Incorrect email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }else {
            System.out.println("User role: " + user.getRole());
        }

        try {
            // Wywołanie serwisu w celu zalogowania użytkownika
            UserDto userDto = userService.loginUser(loginRequest);

            // Generowanie tokenu JWT
            String token = jwtTokenProvider.generateToken(userDto);

            // Sprawdzanie czy token został poprawnie wygenerowany
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Error during token generation"));
            }

            response.put("token", token);
            response.put("user", Map.of("id", userDto.getId(), "email", userDto.getEmail()));

            return ResponseEntity.ok(response);

        }catch (UserNotFoundException ex){
            response.put("message", "Incorrect email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }catch (Exception ex){
            // Obsługa innych wyjątków
            response.put("message", "An error occurred during login");
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
