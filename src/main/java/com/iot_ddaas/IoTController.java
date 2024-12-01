package com.iot_ddaas;

import com.iot_ddaas.frontend.auth.CustomUserDetails;
import com.iot_ddaas.sensors.Sensor;
import com.iot_ddaas.sensors.SoilMoistureSensor;
import com.iot_ddaas.sensors.TemperatureSensor;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.service.EmailService;
import com.iot_ddaas.service.IoTDataService;
import com.iot_ddaas.service.UserService;

import jakarta.mail.MessagingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;

import java.util.*;

/**
 * IoTController obsługuje żądania związane z danymi, anomaliami i monitorowaniem stanu urządzeń. Zapewnia endpointy do pobierania,
 * usuwania i zapisywania danych, a także wykrywania i obsługi anomalii.
 */
@RestController
@RequestMapping("/iot")
public class IoTController {

    private static final Log log = LogFactory.getLog(IoTController.class);
    private static final Logger logger = LoggerFactory.getLogger(IoTController.class);

    @Autowired
    private IoTDataRepository dataRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private IoTDataService ioTDataService;

    private final Set<String> notifiedDevices = new HashSet<>();
    private boolean isFirstRun = true;

    /**
     * Endpoint do pobierania wszystkich danych dla określonego userId.
     */
    @GetMapping("/data")
    public ResponseEntity<List<IoTData>> getAllData(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = (customUserDetails !=null) ? customUserDetails.getId() :  null;
        List<IoTData> data;

        if (userId != null){
            data = dataRepository.findByUserId(userId);
        }else {
            data = dataRepository.findAll();
        }

        return ResponseEntity.ok(data);
    }

    /**
     * Endpoint do pobierania określonych danych według id i userId.
     */
    @GetMapping("/data/{id}")
    public ResponseEntity<IoTData> getDataById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long userId = customUserDetails.getId();
        Optional<IoTData> data = dataRepository.findByIdAndUserId(id, userId);
        return data.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    /**
     * Endpoint do usuwania określonego rekordu danych na podstawie id i userId.
     */
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

    /**
     * Endpoint do pobierania danych o wilgotności gleby z ostatnich 12 godzin dla określonego użytkownika.
     */
    @GetMapping("/soil-moisture/realtime")
    public List<IoTData> getSoilMoistureData(@RequestParam Long userId){
        return ioTDataService.getSoilMoistureDataForLast12Hours(userId);
    }

    /**
     * Endpoint do pobierania danych o temperaturze dla określonego użytkownika.
     */
    @GetMapping("/temperature/realtime")
    public List<IoTData> getTemperatureData(@RequestParam Long userId){
        return ioTDataService.getLatestTemperatureData(userId);
    }

    /**
     * Endpoint do pobierania wszystkich anomalii.
     */
    @GetMapping("/anomalies")
    public List<Anomaly> getAllAnomalies() {
        return anomalyRepository.findAll();
    }

    /**
     * Endpoint do pobierania historycznych danych dla użytkownika w określonym zakresie czasu.
     */
    @GetMapping("/historical-data")
    public ResponseEntity<List<IoTData>> getHistoricalData(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        try {
            // Parsowanie dat
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);

            // Pobieranie danych historycznych na podstawie userId i zakresu czasu.
            List<IoTData> data = dataRepository.findByUserIdAndTimestampBetween(userId, start, end);

            return ResponseEntity.ok(data);
        } catch (DateTimeException e){
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e){
            logger.info("Error while downloading historical data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint do usuwania określonej anomalii według jej id.
     */
    @DeleteMapping("/anomalies/{id}")
    public ResponseEntity<String> deleteAnomaly(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long userId = customUserDetails.getId();
        System.out.println("UserId:" + userId);

        Optional<Anomaly> anomaly = anomalyRepository.findById(id);
        if (anomaly.isPresent()) {
            anomalyRepository.deleteById(id);
            return ResponseEntity.ok("Anomaly deleted");
        } else {
            return ResponseEntity.status(403).body("Unauthorized access");
        }
    }

    /**
     * Endpoint do odbierania danych z czujników i przechowywania ich w bazie danych.
     */
    @PostMapping("/data")
    public ResponseEntity<String> receiveData(@RequestBody IoTData data) {

        // Sprawdzanie poprawności otrzymanych danych
        if (data.getDeviceId() == null || (data.getSensor1() == null && data.getTemperatureSensor() == null)){
            return ResponseEntity.badRequest().body("Incorrect input data");
        }

        Long userId = determineUserId(data.getDeviceId());
        if (userId == null) {
            return ResponseEntity.badRequest().body("Unknown deviceId");
        }

        data.setUserId(userId);
        data.setTimestamp(LocalDateTime.now().truncatedTo((ChronoUnit.SECONDS)));
        log.info("Received data: " + data);

        if (data.getSensor1() != null) {
            log.info("Sensor 1: " + data.getSensor1());
        }
        if (data.getSensor2() != null) {
            log.info("Sensor 2: " + data.getSensor2());
        }
        if (data.getTemperatureSensor() != null) {
            log.info("Temperature sensor: " + data.getTemperatureSensor());
        }

        // Zapisywanie danych i wykrywanie anomalii
        dataRepository.save(data);
        dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());
        detectAnomalies(data);
        return ResponseEntity.ok("The data has been accepted");
    }

    // Metoda do określenia userId na podstawie deviceId
    private Long determineUserId(String deviceId) {
        return switch (deviceId) {
            case "ESP-32-moisture-sensors" -> 1L;
            case "ESP8266-temperature-sensor" -> 2L;
            default -> null;
        };
    }

    // Metoda do zapisywania danych, aktualizowania czasu ostatniego odczytu i wykrywania anomalii.
    public void saveData(IoTData data) {
        dataRepository.save(data);
        dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());
        detectAnomalies(data);
    }

    // Zadanie sprawdzające awarie urządzenia co 2 minuty
    @Scheduled(fixedRate = 120000)
    public void checkForDeviceFailures() {
        List<IoTData> devices = dataRepository.findAll();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

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

    // Obsługa awarii urządzenia poprzez utworzenie anomalii i wysłanie powiadomienia.
    private void handleDeviceFailure(String deviceId, Long userId, LocalDateTime now) {

        if (!notifiedDevices.contains(deviceId)){
            log.warn("Device failure: " + deviceId);
            Optional<Anomaly> lastAnomaly = anomalyRepository.findTopByDeviceIdOrderByTimestampDesc(deviceId);

            if (lastAnomaly.isPresent() && lastAnomaly.get().getTimestamp().isAfter(now.minusMinutes(1))){
                return;
            }

            Anomaly anomaly = new Anomaly();
            anomaly.setDeviceId(deviceId);
            anomaly.setType("No data from the device for one minute");
            anomaly.setTimestamp(now.truncatedTo(ChronoUnit.SECONDS));
            anomalyRepository.save(anomaly);
            sendAnomalyNotification(userId, deviceId, "Anomaly has been detected for the user " + userId + ", device " + deviceId + ".");
            notifiedDevices.add(deviceId);
        }
    }

    // Wysyłanie powiadomień o anomaliach do użytkowników
    private void sendAnomalyNotification(Long userId, String deviceId, String message){
        try {
            emailService.sendAnomalyNotification(userId, deviceId, message);
        }catch (MessagingException e){
            log.error("Failed to send anomaly notification", e);
        }
    }

    // Wykrywanie anomalii na podstawie deviceId
    private void detectAnomalies(IoTData data){
        Long userId = data.getUserId();
        String deviceId = data.getDeviceId();

        if ("ESP-32-moisture-sensors".equals(deviceId)){
            detectSoilMoistureAnomalies(data, userId);
        } else if ("ESP8266-temperature-sensor".equals(deviceId)) {
            detectTemperatureAnomalies(data, userId);
        }
    }

    // Wykrywanie anomalii w danych z czujników wilgotności gleby
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

    // Sprawdzanie anomalii czujnika wilgotności gleby
    private void checkSoilMoistureSensor(SoilMoistureSensor sensor, Integer sensorValue, Long userId){
        if (sensorValue == null){
            log.warn("No moisture reading, failure.");
            createAndNotifyAnomaly(sensor, "Failure of microcontroller or soil moisture sensors, no reading.", userId);
        }else {
            if (sensor.detectAnomaly(10)){
                createAndNotifyAnomaly(sensor, "Sensor value is below the set threshold value", userId);
            }
        }
    }

    // Wykrywanie anomalii w danych z czujników temperatury
    private void detectTemperatureAnomalies(IoTData data, Long userId) {
        TemperatureSensor temperatureSensor = new TemperatureSensor();
        temperatureSensor.setDeviceId(data.getDeviceId());
        temperatureSensor.setTimestamp(data.getTimestamp());
        temperatureSensor.setTemperature(data.getTemperatureSensor());
        temperatureSensor.setUserId(userId);

        if (data.getTemperatureSensor() == null) {
            log.warn("No temperature reading, failure.");
            createAndNotifyAnomaly(temperatureSensor, "Temperature sensor failure, no reading.", userId);
        } else {
            if (temperatureSensor.detectAnomaly(40)) {
                createAndNotifyAnomaly(temperatureSensor, "Temperature sensor: ", userId);
            }
        }
    }

    // Tworzenie powiadomień o anomaliach
    private void createAndNotifyAnomaly(Sensor sensor, String message, Long userId){
        Anomaly anomaly = sensor.createAnomaly(message);
        anomalyRepository.save(anomaly);
        sendAnomalyNotification(userId, sensor.getDeviceId(), message);
    }
}
