package auth;

import com.iot_ddaas.Main;
import com.iot_ddaas.frontend.auth.*;
import com.iot_ddaas.frontend.auth.token.JwtAuthenticationFilter;
import com.iot_ddaas.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private User user;

    @BeforeEach
    @Rollback
    void setUp(){
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();

        user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setUsername("user");
        user.setRole("ROLE_USER");
        userRepository.save(user);
    }

    @AfterEach
    @Rollback
    void tearDown(){
        userRepository.delete(user);
    }

    @Test
    void shouldSetUserPrincipalInSecurityContext() throws Exception{
        // Tworzenie instancji CustomUserDetails
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // Tworzenie pustego kontekstu bezpieczeństwa
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Tworzenie instancji Authentication z CustomUserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // Ustawnianie kontekstu bezpieczeństwa z odpowiednią autoryzacją
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Pobieranie autoryzacji z SecurityContextHolder
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());

        // Pobieranie Principal i rzutowanie na CustomUserDetails
        CustomUserDetails userPrincipal = (CustomUserDetails) auth.getPrincipal();
        assertNotNull(userPrincipal);
        assertEquals("test@gmail.com", userPrincipal.getUsername());
    }

    @Test
    @Transactional
    public void shouldAuthenticateUserAndReturnUserPrincipal() throws Exception {

        // Ustawienie MockMvc z kontekstem aplikacji i filtrami
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(jwtAuthenticationFilter)
                .build();

        // Symulacja logowania użytkownika
        MvcResult mvcResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@gmail.com\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // Wydobycie tokenu z odpowiedzi
        String token = JsonPath.read(mvcResult.getResponse().getContentAsString(),"$.token");

        // Ustawienie SecurityContext
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Wykonanie zapytania do endpointu /iot/data z nagłówkiem autoryzacji
        mockMvc.perform(get("/iot/data")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk()) // Oczekiwanie statusu 200 OK
                .andExpect(result -> {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // Pobieranie autoryzacji z SecurityContextHolder
                    System.out.println("Authentication after request: " + auth);
                    assertNotNull(auth, "Authentication should not be null after request"); // Sprawdzenie czy autoryzacja nie jest pusta
                    assertTrue(auth.isAuthenticated(), "User should be authenticated after request"); // Sprawdzenie czy użytkownik jest uwierzytelniony

                    // Pobieranie principal i sprawdzenie jego poprawności
                    CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
                    System.out.println("Principal after request: " + principal);
                    assertEquals("test@gmail.com", principal.getUsername(), "Email should match the expected value"); // Porównanie email
                })
                .andExpect(jsonPath("$").isArray());
    }
}
