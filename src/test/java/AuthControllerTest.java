import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot_ddaas.Main;
import com.iot_ddaas.frontend.auth.LoginRequest;
import com.iot_ddaas.frontend.auth.User;
import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.repository.UserRepository;
import com.iot_ddaas.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    @Rollback
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Transactional
    public void shouldLoginSuccessfullyAndReturnTokenAndUserDetails() throws Exception {

        String email = "test@gmail.com";
        String rawPassword = "password";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setPassword(passwordEncoder.encode(rawPassword)); // Zaszyfrowane hasło
        mockUser.setUsername("user");

        userRepository.save(mockUser);

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
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.email").value(email));


        User userFromDb = userRepository.findByEmail(email);
        assertNotNull(userFromDb);
        assertEquals(email, userFromDb.getEmail());
        assertTrue(passwordEncoder.matches(rawPassword, userFromDb.getPassword()));
    }

    @Test
    @Transactional
    public void shouldFailLoginWithInvalidEmail() throws Exception{

        // Przygotowywanie LoginRequest dla nieprawidłowego emaila
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalid_email@gmail.com");
        loginRequest.setPassword("password");

        ObjectMapper objectMapper = new ObjectMapper();
        String loginRequestedJson = objectMapper.writeValueAsString(loginRequest);

        // Wykonanie żądania POST do endpointa logowania
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestedJson))
                .andExpect(status().isUnauthorized()) // Sprawdzanie że odpowiedź HTTP ma status 401 Unauthorized
                .andExpect(jsonPath("$.message").value("Nieprawidłowy email lub hasło"));
    }

/*
    @Test
    @Transactional
    public void testAccessProtectedResource() throws Exception{

        String email = "test@gmail.com";
        String rawPassword = "password";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setPassword(passwordEncoder.encode(rawPassword)); // Zaszyfrowane hasło
        mockUser.setUsername("user");

        userRepository.save(mockUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        // Wykonanie żądania POST do endpointa logowania, aby uzyskać token JWT
        ObjectMapper objectMapper = new ObjectMapper();
        String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

        String jwtToken = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonResponse = objectMapper.readTree(jwtToken);
        String token = jsonResponse.get("token").asText();
        System.out.println("JWT Token: " + token);

        // Wykonanie żądania GET do chronionego zasobu tokenem JWT
        mockMvc.perform(get("/iot/data")
                .header("Authorization", "Bearer " + token)) // Dodanie tokena do nagłówków
                .andExpect(status().isOk());
    }

 */
}
