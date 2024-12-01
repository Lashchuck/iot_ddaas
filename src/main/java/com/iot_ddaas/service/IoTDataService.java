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

/**
 * Klasa usługi odpowiedzialna za zarządzanie danymi i anomaliami.
 * Współdziała z repozytoriami w celu pobierania i manipulowania danymi, wykrywania anomalii i wykonywania operacji CRUD.
 */
@Service
public class IoTDataService {

    @Autowired
    private IoTDataRepository dataRepository;

    @Autowired
    private AnomalyRepository anomalyRepository;

    // Pobieranie wszystkich danych dla określonego userId.
    public List<IoTData> getAllData(Long userId) {
        return dataRepository.findByUserId(userId);
    }

    // Pobieranie określonego rekordu danych według id dla określonego userId.
    public Optional<IoTData> getDataById(Long id, Long userId) {
        return dataRepository.findByIdAndUserId(id, userId);
    }

    // Zapisywanie dostarczonych danych w repozytorium i sprawdzanie anomalii.
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

    // Usunięcie rekordu danych o podanym id z repozytorium.
    public void deleteData(Long id) {
        dataRepository.deleteById(id);
    }

    // Pobieranie wszystkie anomalii z repozytorium anomalii.
    public List<Anomaly> getAllAnomalies() {
        return anomalyRepository.findAll();
    }

    // Usunięcie anomalii o podanym id z repozytorium.
    public void deleteAnomaly(Long id) {
        anomalyRepository.deleteById(id);
    }

    // Wykrywanie anomalii na podstawie dostarczonych danych.
    private void detectAnomalies(IoTData data) {}
}
