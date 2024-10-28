package auth;

import com.iot_ddaas.Main;
import com.iot_ddaas.frontend.auth.UserDto;
import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
public class TokenTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDto mockUserDto;

    @BeforeEach
    public void setUp(){
        // Tworzenie mockowanego użytkownika
        mockUserDto = new UserDto();
        mockUserDto.setId(1L);
        mockUserDto.setEmail("test@gmail.com");
        mockUserDto.setPassword(passwordEncoder.encode("password")); // Zaszyfrowane hasło
        mockUserDto.setUsername("user");
        mockUserDto.setRole("ROLE_USER");
    }

    @Test
    public void shouldInitializeSecretKey() {
        assertNotNull(jwtTokenProvider.getSecretKey(), "SecretKey should be initialized.");
    }

    @Test
    public void shouldGenerateTokenAndValidateEmail(){
        // Generowanie tokenu JWT
        String token = jwtTokenProvider.generateToken(mockUserDto);

        // Sprawdzanie, czy token jest poprawnie wygenerowany
        assertNotNull(token);

        // Walidacja zawartości tokenu
        Optional<String> emailFromToken = jwtTokenProvider.getEmailFromToken(token);
        assertTrue(emailFromToken.isPresent(), "Email should be present in the token");
        assertEquals(mockUserDto.getEmail(), emailFromToken.get());
    }

    @Test
    public void shouldValidateTokenSuccessfully(){
        // Generowanie tokenu JWT dla UserDto
        String token = jwtTokenProvider.generateToken(mockUserDto);

        // Sprawdzanie, czy token jest poprawnie walidowany
        assertTrue(jwtTokenProvider.isTokenValid(token));
    }
}
