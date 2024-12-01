import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot_ddaas.IoTData;
import com.iot_ddaas.Main;
import com.iot_ddaas.frontend.auth.User;
import com.iot_ddaas.frontend.auth.UserDto;
import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.repository.UserRepository;
import com.iot_ddaas.service.IoTDataService;
import com.iot_ddaas.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Rollback
public class IoTControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IoTDataRepository ioTDataRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IoTDataService ioTDataService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long userId;
    private User user;
    private Authentication authentication;


    @BeforeEach
    void setUp(){
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setUsername("user");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        userId = user.getId();
    }

    @Test
    void shouldSaveDataSuccessfully() throws Exception{
        System.out.println("Zarejestrowany userId: " + userId);

        IoTData data = new IoTData();
        data.setUserId(userId); // Użyj ID zarejestrowanego użytkownika
        data.setDeviceId("ESP8266-temperature-sensor");
        data.setTemperatureSensor(25f);
        data.setTimestamp(LocalDateTime.now());
        data.setLastRead(LocalDateTime.now());
        ioTDataRepository.save(data);
        // Serializaja obiektu do formatu JSON
        String json = objectMapper.writeValueAsString(data);
        System.out.println("JSON to send: " + json);

        String token = jwtTokenProvider.generateToken(new UserDto(userId, "user", "test@gmail.com", "password", "ROLE_USER"));

        mockMvc.perform(post("/iot/data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                // .andExpect(status().isBadRequest())
                // .andExpect(content().string("Komunikat błędu: "));
                .andExpect(content().string("The data has been accepted"));
    }

    @Test
    void shouldReturnAllDataForAuthorizedUser() throws Exception{
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        System.out.println("Registered userId: " + userId);

        IoTData ioTData = new IoTData();
        ioTData.setUserId(userId); // Użyj ID zarejestrowanego użytkownika
        ioTData.setDeviceId("ESP-32-moisture-sensors");
        ioTData.setSensor1(10);
        ioTData.setSensor2(20);
        ioTData.setTemperatureSensor(null);
        ioTData.setTimestamp(LocalDateTime.now());
        ioTDataRepository.save(ioTData);

        String token = jwtTokenProvider.generateToken(new UserDto(userId, "user", "test@gmail.com", "password", "ROLE_USER"));

        mockMvc.perform(get("/iot/data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                .andExpect(content().json("[{\"deviceId\":\"ESP-32-moisture-sensors\",\"sensor1\":10,\"sensor2\":20,\"userId\":" + userId + "}]"));
    }
}
