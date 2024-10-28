package auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot_ddaas.Main;
import com.iot_ddaas.frontend.auth.*;
import com.iot_ddaas.frontend.auth.token.JwtAuthenticationFilter;
import com.iot_ddaas.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void shouldFindUserByEmail(){
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("password");
        user.setUsername("testuser");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        User foundUser = userRepository.findByEmail("test@gmail.com");
        assertNotNull(foundUser); // Sprawdzanie czy użytkownik został znaleziony
        assertEquals("test@gmail.com", foundUser.getEmail()); // Sprawdzanie email
        userRepository.delete(user);
    }

    @Test
    @Transactional
    public void shouldLoginSuccessfullyAndReturnTokenAndUserDetails() throws Exception {

        String email = "test@gmail.com";
        String rawPassword = "password";

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword)); // Zaszyfrowane hasło
        user.setUsername("user");
        userRepository.save(user);

        // Przygotowanie LoginRequest
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        ObjectMapper objectMapper = new ObjectMapper();
        String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

        // Wykonanie żądania POST do endpointa logowania
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.email").value(email));

        User userFromDb = userRepository.findByEmail(email);
        assertNotNull(userFromDb);
        assertEquals(email, userFromDb.getEmail());
        assertTrue(passwordEncoder.matches(rawPassword, userFromDb.getPassword()));

        userRepository.delete(user);
        userRepository.delete(userFromDb);
    }

    @Test
    @Transactional
    public void shouldFailLoginWithInvalidEmail() throws Exception{

        String email = "invalid_email@gmail.com";
        String password = "password";

        // Przygotowywanie LoginRequest dla nieprawidłowego emaila
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        ObjectMapper objectMapper = new ObjectMapper();
        String loginRequestedJson = objectMapper.writeValueAsString(loginRequest);

        // Wykonanie żądania POST do endpointa logowania
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestedJson))
                .andExpect(status().isUnauthorized()) // Sprawdzanie że odpowiedź HTTP ma status 401 Unauthorized
                .andExpect(jsonPath("$.message").value("Nieprawidłowy email lub hasło"));
    }
}
