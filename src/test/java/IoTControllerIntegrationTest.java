import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot_ddaas.IoTData;
import com.iot_ddaas.Main;
import com.iot_ddaas.repository.IoTDataRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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

    @BeforeEach
    void setUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testGetAllData() throws Exception{
        IoTData data = new IoTData(null, "ESP-32-moisture-sensors", 20, 30, 1L, null, null);
        ioTDataRepository.save(data);

        mockMvc.perform(get("/iot/data")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(content().string("Dane zostały przyjęte"))
                .andExpect(jsonPath("$[0].deviceId").value("ESP-32-moisture-sensors"))
                .andExpect(jsonPath("$[0].sensor1").value(20));

        // Wysyłanie żądania GET do API i sprawdzenie odpowiedzi
        MvcResult result = mockMvc.perform(get("/iot/data")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);

        if (content.isEmpty()){
            System.out.println("Odpowiedź jest pusta");
        }

        mockMvc.perform(get("/iot/data")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceId").value("ESP-32-moisture-sensors"))
                .andExpect(jsonPath("$[0].sensor1").value(20));
    }

    @Test
    void testSaveData() throws Exception{
        IoTData data = new IoTData(null, "ESP8266-temperature-sensor", null, null, 1L, null, null);

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
}
