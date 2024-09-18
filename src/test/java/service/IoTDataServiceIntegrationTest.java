package service;

import com.iot_ddaas.Anomaly;
import com.iot_ddaas.IoTData;
import com.iot_ddaas.Main;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.service.IoTDataService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
public class IoTDataServiceIntegrationTest {


    @Autowired
    private IoTDataRepository dataRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;

    @Autowired
    private IoTDataService ioTDataService;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT schema_name FROM information_schema.schemata;");
            while (resultSet.next()) {
                System.out.println("Existing schema: " + resultSet.getString("schema_name"));
            }
            // Tworzenie schematu test_schema, jeśli jeszcze nie istnieje
            statement.execute("CREATE SCHEMA IF NOT EXISTS test_schema;");
            // Przypisanie search_path do test_schema
            statement.execute("SET search_path TO test_schema;");

            // Tworzenie tabel na podstawie encji
            System.out.println("Schema and tables created.");
        }
    }

    @Test
    void testSaveAndRetrieveData(){

        // Zapis i odczyt danych
        IoTData data = new IoTData(null, "ESP-32-moisture-sensors", 20, 30, 1L, null, null);
        ioTDataService.saveData(data);

        List<IoTData> savedData = ioTDataService.getAllData();
        assertEquals(1, savedData.size());
        assertEquals("ESP-32-moisture-sensors", savedData.get(0).getDeviceId());
    }

    @Test
    void testSaveAndRetrieveAnomaly(){

        Anomaly anomaly = new Anomaly(null, "ESP8266-temperature-sensor", null, "Awaria", 1L, null);
        anomalyRepository.save(anomaly);

        List<Anomaly> savedAnomalies = anomalyRepository.findAll();
        assertEquals(1, savedAnomalies.size());
        assertEquals("ESP8266-temperature-sensor", savedAnomalies.get(0).getDeviceId());
    }
}
