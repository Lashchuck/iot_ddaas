package com.iot_ddaas.service;

import com.iot_ddaas.frontend.auth.LoginRequest;
import com.iot_ddaas.frontend.auth.User;
import com.iot_ddaas.frontend.auth.UserDto;
import com.iot_ddaas.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Inicjalizacja mechanizmu do szyfrowania haseł BCrypt
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registerUser(UserDto userDto){
        // Logika rejestracji
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        // Szyfrowanie hasła przed zapisem w bazie
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        // Zapis użytkownika w bazie danych
        userRepository.save(user);
    }

    public class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
    public UserDto loginUser(LoginRequest loginRequest){
        // Znalezienie użytkownika po emailu
        User user = userRepository.findByEmail(loginRequest.getEmail());

        // Sprawdzanie czy użytkownik istnieje i czy podane hasło jest poprawne
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            // Jeśli dane są poprawne zostaje zwracane DTO bez hasła
            return new UserDto(user.getId(), user.getUsername(), user.getEmail());
        }else {
            throw new InvalidCredentialsException("Nieprawidłowy email lub hasło");
        }
    }
}
