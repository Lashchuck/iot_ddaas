package com.iot_ddaas.service;

import com.iot_ddaas.Anomaly;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.IoTData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IoTDataService {

    @Autowired
    private IoTDataRepository dataRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;

    public List<IoTData> getAllData(Long userId) {
        return dataRepository.findByUserId(userId);
    }

    public Optional<IoTData> getDataById(Long id, Long userId) {
        return dataRepository.findByIdAndUserId(id, userId);
    }

    public void saveData(IoTData data) {
        dataRepository.save(data);
        detectAnomalies(data);
    }

    // Pobieranie danych z czujników wilgotności gleby z ostatnich 12 godzin
    public List<IoTData> getSoilMoistureDataForLast12Hours(Long userId){
        LocalDateTime twelveHoursAgo = LocalDateTime.now().minusHours(12);
        return dataRepository.findByUserIdAndTimestampAfter(userId, twelveHoursAgo);
    }

    // Pobieranie najnowszej wartości czujnika temperatury
    public List<IoTData> getLatestTemperatureData(Long userId){
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        return dataRepository.findByUserIdAndTimestampAfter(userId, twentyFourHoursAgo);
    }
    public void deleteData(Long id) {
        dataRepository.deleteById(id);
    }

    public List<Anomaly> getAllAnomalies() {
        return anomalyRepository.findAll();
    }

    public void deleteAnomaly(Long id) {
        anomalyRepository.deleteById(id);
    }

    private void detectAnomalies(IoTData data) {}
}
