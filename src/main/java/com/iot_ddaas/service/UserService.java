package com.iot_ddaas.service;

import com.iot_ddaas.frontend.auth.*;
import com.iot_ddaas.repository.UserRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


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
    }

    public User save(User user){
        return userRepository.save(user);
    }

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null){
            logger.error("Użytkownik {} nie został znaleziony", username);
            throw new UsernameNotFoundException("Użytkownik nie został znaleziony");
        }
        logger.info("Znaleziony użytkownik: {}", user.getUsername());
        return new CustomUserDetails(user);
    }


    public UserDto loginUser(LoginRequest loginRequest){
        logger.info("Próba logowania użytkownika z email: {}", loginRequest.getEmail());
        // Znalezienie użytkownika po emailu
        User user = userRepository.findByEmail(loginRequest.getEmail());


        // Sprawdzanie czy użytkownik istnieje i czy podane hasło jest poprawne
        if (user == null){
            logger.warn("Użytkownik nie znaleziony: {}", loginRequest.getEmail());
            // Jeśli dane są poprawne zostaje zwracane DTO bez hasła
            throw new UserNotFoundException("Nieprawidłowy email lub hasło");
        }else {
            logger.info("Znaleziono użytkownika: {}", user.getEmail());
        }

        if (passwordEncoder == null){
            logger.error("PasswordEncoder is null");
            throw new UserNotFoundException("PasswordEncoder nie został zainicjalozowany");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            logger.warn("Nieprawidłowe hasło dla użytkownika: {}", loginRequest.getEmail());
            throw new UserNotFoundException("Nieprawidłowy email lub hasło");
        }
        logger.info("Użytkownik zalogowany pomyślnie: {}", user.getUsername());
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    public User findByEmail(String email){
        logger.info("Wykonuję zapytanie do bazy danych o użytkownika z emailem: {}", email);
        return userRepository.findByEmail(email);
    }

    public User findUserByEmailOrThrow(String email){
        User user = userRepository.findByEmail(email);
        if (user == null){
            throw new UserNotFoundException("Użytkownik o podanym adresie email nie został znaleziony");
        }
        return user;
    }
}
