package com.iot_ddaas;

import com.iot_ddaas.sensors.Sensor;
import com.iot_ddaas.sensors.SoilMoistureSensor;
import com.iot_ddaas.sensors.TemperatureSensor;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.service.EmailService;

import jakarta.mail.MessagingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/iot")
public class IoTController {

    private static final Log log = LogFactory.getLog(IoTController.class);

    @Autowired
    private IoTDataRepository dataRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;

    @Autowired
    private EmailService emailService;

    private final Set<String> notifiedDevices = new HashSet<>();
    private boolean isFirstRun = true;

    // Pobieranie wszystkich danych tylko dla danego użytkownika.
    // Endpoint zwraca listę danych IoT dla danego użytkownika na podstawie przekazanego userId
    @GetMapping("/data")
    public List<IoTData> getAllData(@AuthenticationPrincipal Long userId) {
        return dataRepository.findByUserId(userId);
    }

    // Pobieranie konkretnej wartości z tabeli iotdata na podstawie id i userId
    @GetMapping("/data/{id}")
    public ResponseEntity<IoTData> getDataById(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        Optional<IoTData> data = dataRepository.findByIdAndUserId(id, userId);
        return data.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Usuwanie danych danego użytkownika
    @DeleteMapping("/data/{id}")
    public ResponseEntity<String> deleteData(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        Optional<IoTData> data = dataRepository.findByIdAndUserId(id, userId);
        if (data.isPresent()) {
            dataRepository.deleteById(id);
            return ResponseEntity.ok("Data deleted");
        } else {
            return ResponseEntity.status(403).body("Unauthorized access");
        }
    }

    // Pobieranie wszystkich anomalii
    @GetMapping("/anomalies")
    public List<Anomaly> getAllAnomalies() {
        return anomalyRepository.findAll();
    }

    // Pobieranie danych dla danego userId w wybranym okresie czasu
    @GetMapping("/historical-data")
    public List<IoTData> getHistoricalData(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        // Pobieranie danych dla danego userId w wybranym okresie czasu
        return dataRepository.findByUserIdAndTimestampBetween(userId, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    // Usuwanie anomalii
    @DeleteMapping("/anomalies/{id}")
    public ResponseEntity<String> deleteAnomaly(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        Optional<Anomaly> anomaly = anomalyRepository.findByIdAndUserId(id, userId);
        if (anomaly.isPresent()) {
            anomalyRepository.deleteById(id);
            return ResponseEntity.ok("Anomaly deleted");
        } else {
            return ResponseEntity.status(403).body("Unauthorized access");
        }
    }

    // Odbieranie i przetwarzanie danych
    @PostMapping("/data")
    public ResponseEntity<String> receiveData(@RequestBody IoTData data) {

        if (data.getDeviceId() == null || (data.getSensor1() == null && data.getTemperatureSensor() == null)){
            log.error("Niepoprawne dane wejściowe: " + data);
            return ResponseEntity.badRequest().body("Niepoprawne dane wejściowe");
        }

        Long userId = determineUserId(data.getDeviceId());
        if (userId == null) {
            return ResponseEntity.badRequest().body("Nieznane deviceId");
        }

        data.setUserId(userId);
        dataRepository.save(data);
        dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());
        detectAnomalies(data);
        return ResponseEntity.ok("Dane zostały przyjęte");
    }

    private Long determineUserId(String deviceId) {
        return switch (deviceId) {
            case "ESP-32-moisture-sensors" -> 1L;
            case "ESP8266-temperature-sensor" -> 2L;
            default -> null;
        };
    }

    public void saveData(IoTData data) {
        dataRepository.save(data);
        dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());
        detectAnomalies(data);
    }

    @Scheduled(fixedRate = 120000)
    public void checkForDeviceFailures() {
        List<IoTData> devices = dataRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (IoTData device : devices) {
            LocalDateTime lastRead = device.getLastRead();
            String deviceId = device.getDeviceId();
            Long userId = device.getUserId();

            if (lastRead != null) {
                if (lastRead.isAfter(now.plusMinutes(2))) {
                    continue;
                }
            }

            if (isFirstRun){
                if(lastRead != null && lastRead.isBefore(now.minusMinutes(2))){
                    continue;
                }
            }

            // Sprawdzanie, czy urządzenie nie wysłało danych w ciągu ostatniej minuty
            boolean isDeviceFailing = lastRead == null || lastRead.isBefore(now.minusMinutes(1));

            if (isDeviceFailing) {
                handleDeviceFailure(deviceId, userId, now);
            } else {
                // Usuwanie z flagi, gdy urządzenie wysłało dane
                notifiedDevices.remove(deviceId);
            }
            isFirstRun = false;
        }
    }

    private void handleDeviceFailure(String deviceId, Long userId, LocalDateTime now) {

        if (!notifiedDevices.contains(deviceId)){
            log.warn("Awaria urządzenia: " + deviceId);
            Optional<Anomaly> lastAnomaly = anomalyRepository.findTopByDeviceIdOrderByTimestampDesc(deviceId);

            if (lastAnomaly.isPresent() && lastAnomaly.get().getTimestamp().isAfter(now.minusMinutes(1))){
                return;
            }

            Anomaly anomaly = new Anomaly();
            anomaly.setDeviceId(deviceId);
            anomaly.setType("Brak danych od urządzenia przez minutę");
            anomaly.setTimestamp(now);
            anomalyRepository.save(anomaly);
            sendAnomalyNotification(userId, deviceId, "Anomaly has been detected for the user " + userId + ", device " + deviceId + ".");
            notifiedDevices.add(deviceId);
        }
    }

    private void sendAnomalyNotification(Long userId, String deviceId, String message){
        try {
            emailService.sendAnomalyNotification(userId, deviceId, message);
        }catch (MessagingException e){
            log.error("Failed to send anomaly notification", e);
        }
    }

    private void detectAnomalies(IoTData data){
        Long userId = data.getUserId();
        String deviceId = data.getDeviceId();

        if ("ESP-32-moisture-sensors".equals(deviceId)){
            detectSoilMoistureAnomalies(data, userId);
        } else if ("ESP8266-temperature-sensor".equals(deviceId)) {
            detectTemperatureAnomalies(data, userId);
        }
    }

    private void detectSoilMoistureAnomalies(IoTData data, Long userId){
        SoilMoistureSensor sensor1 = new SoilMoistureSensor();
        sensor1.setDeviceId(data.getDeviceId());
        sensor1.setTimestamp(data.getTimestamp());
        sensor1.setMoistureValue(data.getSensor1());
        sensor1.setUserId(userId);
        checkSoilMoistureSensor(sensor1, data.getSensor1(), userId);

        SoilMoistureSensor sensor2 = new SoilMoistureSensor();
        sensor2.setDeviceId(data.getDeviceId());
        sensor2.setTimestamp(data.getTimestamp());
        sensor2.setMoistureValue(data.getSensor2());
        sensor2.setUserId(userId);
        checkSoilMoistureSensor(sensor2, data.getSensor2(), userId);
    }

    private void checkSoilMoistureSensor(SoilMoistureSensor sensor, Integer sensorValue, Long userId){
        if (sensorValue == null){
            log.warn("Brak odczytu wilgotności, awaria.");
            createAndNotifyAnomaly(sensor, "Awaria mikrokontrolera lub czujników wilgotności gleby, brak odczytu.", userId);
        }else {
            log.info("Czujnik: " + sensor.getMoistureValue());
            if (sensor.detectAnomaly(10)){
                createAndNotifyAnomaly(sensor, "Czujnik", userId);
            }
        }
    }

    private void detectTemperatureAnomalies(IoTData data, Long userId) {
        TemperatureSensor temperatureSensor = new TemperatureSensor();
        temperatureSensor.setDeviceId(data.getDeviceId());
        temperatureSensor.setTimestamp(data.getTimestamp());
        temperatureSensor.setTemperature(data.getTemperatureSensor());
        temperatureSensor.setUserId(userId);

        if (data.getTemperatureSensor() == null) {
            log.warn("Brak odczytu temperatury, awaria.");
            createAndNotifyAnomaly(temperatureSensor, "Awaria czujnika temperatury, brak odczytu.", userId);
        } else {
            log.info("Temperatura: " + data.getTemperatureSensor());
            if (temperatureSensor.detectAnomaly(40)) {
                createAndNotifyAnomaly(temperatureSensor, "Czujnik temperatury", userId);
            }
        }
    }

    private void createAndNotifyAnomaly(Sensor sensor, String message, Long userId){
        Anomaly anomaly = sensor.createAnomaly(message);
        anomalyRepository.save(anomaly);
        sendAnomalyNotification(userId, sensor.getDeviceId(), message);
    }

}
