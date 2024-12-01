package com.iot_ddaas.repository;

import com.iot_ddaas.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Interfejs repozytorium do obsługi operacji związanych z klasą Anomaly.
 * Interfejs rozszerza JpaRepository, które zapewnia podstawowe operacje CRUD dla Anomaly.
 */
@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {


    //Pobieranie listy anomalii powiązanych z określonym userId.
    List<Anomaly> findByUserId(Long userId);

    //Pobieranie anomali według id i userID.
    Optional<Anomaly> findByIdAndUserId(Long id, Long userId);

    //Pobieranie najnowszej anomalii dla określonego urządzenia, uporządkowaną malejąco według czasu.
    Optional<Anomaly> findTopByDeviceIdOrderByTimestampDesc(String deviceId);

    //Sprawdzanie, czy anomalia o podanym timestampie istnieje w bazie danych.
    boolean existsByTimestamp(LocalDateTime timestamp);
}
