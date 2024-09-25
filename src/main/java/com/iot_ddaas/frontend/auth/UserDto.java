package com.iot_ddaas.frontend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
Metoda tworzy obiekt tej klasy z przekazanych argumentów.
DTO (Data Transfer Object) - obiekt służący do komunikacji między frontendem a backhandem.
Zawiera tylko potrzebne do przesłania do użytkownika lub z aplikacji dane.
Nie zawiera haseł w odpowiedzi zwracanej do użytkownika.
 */
public class UserDto {

    private Long id;

    // Pole nie może być puste, message zostanie wyświetlona, jeśli walidacja nie przejdzie, tzn. jeśli username zostanie puste
    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // Konstruktor dla rejestracji, który przyjmuje hasło
    public UserDto(Long id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Konstruktor dla logowania (bez hasła)
    public UserDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Username is required") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Username is required") String username) {
        this.username = username;
    }

    public @Email(message = "Invalid email format") @NotBlank(message = "Email is required") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Password is required") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") String password) {
        this.password = password;
    }
}

