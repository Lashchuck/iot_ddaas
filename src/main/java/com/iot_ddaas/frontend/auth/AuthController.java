package com.iot_ddaas.frontend.auth;

import com.iot_ddaas.frontend.auth.token.JwtResponse;
import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto){
        // Wywołanie serwisu w celu zarejestrowania użytkownika
        userService.registerUser(userDto);
        return ResponseEntity.ok("Użytkownik zarejestrowany");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest){
        // Wywołanie serwisu w celu zalogowania użytkownika
        UserDto user = userService.loginUser(loginRequest);
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // Generowanie tokenu JWT
        String token = jwtTokenProvider.generateToken(user);
        System.out.println("Generated token: " + token);
        if (token == null || token.isEmpty()){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        JwtResponse jwtResponse = new JwtResponse(token, user);
        return ResponseEntity.ok(jwtResponse);
    }
}
