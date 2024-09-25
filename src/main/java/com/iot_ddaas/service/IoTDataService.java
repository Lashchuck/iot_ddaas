package com.iot_ddaas.service;

import com.iot_ddaas.Anomaly;
import com.iot_ddaas.repository.AnomalyRepository;
import com.iot_ddaas.repository.IoTDataRepository;
import com.iot_ddaas.IoTData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
