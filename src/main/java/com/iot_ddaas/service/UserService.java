package com.iot_ddaas.service;

import com.iot_ddaas.frontend.auth.*;
import com.iot_ddaas.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Klasa usługi do zarządzania danymi użytkownika i uwierzytelnianiem.
 * Implementuje UserDetailsService do obsługi szczegółów użytkownika dla Spring Security.
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private final UserRepository userRepository;

    // Inicjalizacja mechanizmu do szyfrowania haseł BCrypt
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("UserService initialized with PasswordEncoder: {}", passwordEncoder);
        System.out.println("UserRepository injected: " + (userRepository !=null));
    }

    // Zapisanie użytkownika w bazie danych.
    public User save(User user){
        return userRepository.save(user);
    }

    /**
     * Rejestracja nowego użytkownika poprzez zakodowanie jego hasła i zapisanie go w bazie danych.
     */
    public void registerUser(UserDto userDto){
        // Logika rejestracji
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Szyfrowanie hasła przed zapisem w bazie
        user.setRole("USER");
        userRepository.save(user); // Zapis użytkownika w bazie danych
    }

    /**
     * Wczytywanie danych użytkownika przez e-mail w celu uwierzytelnienia.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username); // Pobieranie użytkownika przez e-mail
        if (user == null){
            logger.error("User {} not found", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        logger.info("Userfound: {}", user);

        // Przypisanie roli do użytkownika i utworzenie uprawnień
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        System.out.println("Granted authorities for user: " + authorities);

        return new CustomUserDetails(user);
    }

    /**
     * Uwierzytelnienie użytkownika na podstawie adresu e-mail i hasła.
     */
    public UserDto loginUser(LoginRequest loginRequest){
        logger.info("User login attempt from email: {}", loginRequest.getEmail());

        // Znalezienie użytkownika po emailu
        User user = userRepository.findByEmail(loginRequest.getEmail());
        // Sprawdzanie czy użytkownik istnieje i czy podane hasło jest poprawne
        if (user == null){
            logger.warn("User not found: {}", loginRequest.getEmail());
            // Jeśli dane są poprawne zostaje zwracane DTO bez hasła
            throw new UserNotFoundException("Incorrect email or password");
        }

        // Sprawdzanie, czy podane hasło jest zgodne z zapisanym hasłem.
        boolean isPasswordCorrect = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            logger.warn("Incorrect password for user: {}", loginRequest.getEmail());
            throw new UserNotFoundException("Incorrect email or password");
        }

        logger.info("User logged in successfully: {}", user.getUsername());
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    // Wyszukiwanie użytkownika według adresu e-mail.
    public User findByEmail(String email){
        System.out.println("Searching for user with email: " + email);
        return userRepository.findByEmail(email);
    }

    // Wyszukiwanie użytkownika według adresu e-mail, zgłaszając wyjątek, jeśli nie zostanie znaleziony.
    public User findUserByEmailOrThrow(String email){
        User user = userRepository.findByEmail(email);
        System.out.println("Searching for user with email: " + email);
        if (user == null){
            throw new UserNotFoundException("User with the specified email address was not found");
        }
        return user;
    }
}
