package com.iot_ddaas;

import com.iot_ddaas.Sensors.SoilMoistureSensor;
import com.iot_ddaas.Sensors.TemperatureSensor;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/iot")
public class IoTController {

    @Autowired
    private IoTDataRepository dataRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;


    // Pobieranie wszystkich danych tylko dla danego użytkownika
    @GetMapping("/data")
    public List<IoTData> getAllData(@RequestParam Long userId) {
        return dataRepository.findByUserId(userId);
    }

    @GetMapping("/data/{id}")
    public ResponseEntity<IoTData> getDataById(@PathVariable Long id, @RequestParam Long userId){
        Optional<IoTData> data = dataRepository.findByIdAndUserId(id, userId);
        return data.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/data/{id}")
    public ResponseEntity<String> deleteData(@PathVariable Long id, @RequestParam Long userId){
        Optional<IoTData> data = dataRepository.findByIdAndUserId(id, userId);
        if(data.isPresent()){
            dataRepository.deleteById(id);
            return ResponseEntity.ok("Data deleted");
        }else{
            return ResponseEntity.status(403).body("Unauthorized access");
        }
    }

    @GetMapping("/anomalies")
    public List<Anomaly> getAllAnomalies() {
        return anomalyRepository.findAll();
    }

    @DeleteMapping("/anomalies/{id}")
    public ResponseEntity<String> deleteAnomaly(@PathVariable Long id, @RequestParam Long userId) {
        Optional<Anomaly> anomaly = anomalyRepository.findByIdAndUserId(id, userId);
        if (anomaly.isPresent()) {
            anomalyRepository.deleteById(id);
            return ResponseEntity.ok("Anomaly deleted");
        } else {
            return ResponseEntity.status(403).body("Unauthorized access");
        }
    }


    @PostMapping("/data")
    public ResponseEntity<String> receiveData(@RequestBody IoTData data) {

        Long userId;
        if ("ESP-32-moisture-sensors".equals(data.getDeviceId())) {
            userId = 1L; // userId dla ESP32
        } else if ("ESP8266-temperature-sensor".equals(data.getDeviceId())) {
            userId = 2L; // userId dla ESP8266
        } else {
            return ResponseEntity.badRequest().body("Nieznane deviceId");
        }

        // Ustawnienie userId w obiekcie data
        data.setUserId(userId);
        dataRepository.save(data);
        dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());


        detectAnomalies(data);
        return  ResponseEntity.ok("Dane zostały przyjęte");
    }

    public void saveData(IoTData data) {
        dataRepository.save(data);
        dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());
        detectAnomalies(data);
    }

    @Scheduled(fixedRate = 30000)
    public void checkForDeviceFailuers(){
        List<IoTData> devices = dataRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (IoTData device : devices){
            LocalDateTime lastRead = device.getLastRead();
            if (lastRead !=null && lastRead.isBefore(now.minusMinutes(1))){
                System.out.println("Awaria urządzenia: " + device.getDeviceId());
                Anomaly anomaly = new Anomaly();
                anomaly.setDeviceId(device.getDeviceId());
                anomaly.setType("Brak danych od urządzenia przez minutę");
                anomaly.setTimestamp(now);
                anomalyRepository.save(anomaly);
            }
        }
    }

    private void detectAnomalies(IoTData data) {

        Long userId = data.getUserId();
        LocalDateTime now = LocalDateTime.now();

        if (data.getDeviceId().equals("ESP-32-moisture-sensors")){
            SoilMoistureSensor sensor1 = new SoilMoistureSensor();
            sensor1.setDeviceId(data.getDeviceId());
            sensor1.setTimestamp(data.getTimestamp());
            sensor1.setMoistureValue(data.getSensor1());
            sensor1.setUserId(userId);

            if (data.getSensor1() == null || data.getSensor2() == null ){
                System.out.println("Brak odczytu wilgotności, awaria. ");
                Anomaly anomaly = sensor1.createAnomaly("Awaria mikrokontrolera lub czujników wilgotności gleby, brak odczytu.");
                if (!anomalyRepository.existsByTimestamp(anomaly.getTimestamp())) {
                    anomalyRepository.save(anomaly);
                }
            }else {
                System.out.println("Czujnik 1: " + data.getSensor1());
                System.out.println("Czujnik 2: " + data.getSensor2());
            }


            if (sensor1.detectAnomaly(10)){
                Anomaly anomaly = sensor1.createAnomaly("Czujnik 1");
                if (!anomalyRepository.existsByTimestamp(anomaly.getTimestamp())) {
                    System.out.println("Anomalia !!! Czujnik 1: " + data.getSensor1());
                    anomalyRepository.save(anomaly);
                }
            }

            SoilMoistureSensor sensor2 = new SoilMoistureSensor();
            sensor2.setDeviceId(data.getDeviceId());
            sensor2.setTimestamp(data.getTimestamp());
            sensor2.setMoistureValue(data.getSensor2());
            sensor2.setUserId(userId);


            if (sensor2.detectAnomaly(10)){
                Anomaly anomaly = sensor2.createAnomaly("Czujnik 2");
                if (!anomalyRepository.existsByTimestamp(anomaly.getTimestamp())) {
                    System.out.println("Anomalia!!! Czujnik 2: " + data.getSensor2());
                    anomalyRepository.save(anomaly);
                }
            }
        } else if (data.getDeviceId().equals("ESP8266-temperature-sensor")) {
            TemperatureSensor temperatureSensor = new TemperatureSensor();
            temperatureSensor.setDeviceId(data.getDeviceId());
            temperatureSensor.setTimestamp(data.getTimestamp());
            temperatureSensor.setTemperature(data.getTemperatureSensor());
            temperatureSensor.setUserId(userId);


            if (data.getTemperatureSensor() == null){
                System.out.println("Brak odczytu temperatury, awaria. ");
                Anomaly anomaly = temperatureSensor.createAnomaly("Awaria czujnika temepratury, brak odczytu.");
                if (!anomalyRepository.existsByTimestamp(anomaly.getTimestamp())) {
                    anomalyRepository.save(anomaly);
                }
            }else {
                System.out.println("Temperatura: " + data.getTemperatureSensor());
            }

            dataRepository.save(data);
            dataRepository.updateLastRead(data.getDeviceId(), data.getTimestamp());

            if (temperatureSensor.detectAnomaly(40)){
                Anomaly anomaly = temperatureSensor.createAnomaly("Czujnik temperatury");
                if (!anomalyRepository.existsByTimestamp(anomaly.getTimestamp())) {
                    anomalyRepository.save(anomaly);
                }
            }
        }
    }
}
