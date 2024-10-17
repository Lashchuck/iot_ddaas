import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot_ddaas.IoTData;
import com.iot_ddaas.Main;
import com.iot_ddaas.frontend.auth.CustomUserDetails;
import com.iot_ddaas.frontend.auth.LoginRequest;
import com.iot_ddaas.frontend.auth.User;
import com.iot_ddaas.frontend.auth.UserDto;
import com.iot_ddaas.frontend.auth.token.JwtTokenProvider;
import com.iot_ddaas.repository.IoTDataRepository;

import com.iot_ddaas.repository.UserRepository;
import com.iot_ddaas.service.IoTDataService;
import com.iot_ddaas.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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

    private Long userId;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("user", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);

        UserDto userDto = new UserDto(1L, "user", "test@gmail.com", "password");
        userService.registerUser(userDto);
        userId = userService.findByEmail("test@gmail.com").getId();
        System.out.println("Zarejestrowany userId: " + userId);

        IoTData ioTData = new IoTData();
        ioTData.setUserId(userId); // Użyj ID zarejestrowanego użytkownika
        ioTData.setDeviceId("ESP-32-moisture-sensors");
        ioTData.setSensor1(10);
        ioTData.setSensor2(20);
        ioTData.setTemperatureSensor(null);
        ioTData.setTimestamp(LocalDateTime.now());
        ioTDataRepository.save(ioTData);
    }



    @Test
    @WithMockUser(username =  "user", roles = {"USER"})
    void testGetAllData() throws Exception{

        String token = jwtTokenProvider.generateToken(new UserDto(1L, "user", "test@gmail.com", "password"));

        mockMvc.perform(get("/iot/data")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().json("[{\"deviceId\":\"ESP-32-moisture-sensors\",\"sensor1\":10,\"sensor2\":20,\"userId\":" + 1L + "}]"));
    }
/*
    @Test
    void testSaveData() throws Exception{
        IoTData data = new IoTData(null, "ESP8266-temperature-sensor", null, null, 2L, null, null);

        // Serializaja obiektu do formatu JSON
        String json = objectMapper.writeValueAsString(data);

        mockMvc.perform(post("/iot/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                // .andExpect(status().isBadRequest())
                // .andExpect(content().string("Komunikat błędu: "));
                .andExpect(content().string("Dane zostały przyjęte"));
    }

    @Test
    void testSaveDataUnauthorized() throws Exception{
        IoTData data = new IoTData(null, "ESP8266-temperature-sensor", null, null, null, null, null);
        String json = objectMapper.writeValueAsString(data);

        mockMvc.perform(post("/iot/data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nieznane deviceId"));
    }

 */
}
