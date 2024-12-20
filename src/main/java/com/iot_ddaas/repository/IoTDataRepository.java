package com.iot_ddaas.repository;

import com.iot_ddaas.IoTData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


/**
 * Interfejs repozytorium do obsługi operacji związanych z encjami IoTData.
 * Rozszerza JpaRepository, aby zapewnić operacje CRUD dla IoTData.
 */
@Repository
public interface IoTDataRepository extends JpaRepository<IoTData, Long> {

    // Pobieranie wszystkich rekordów danych powiązanych z konkretnym userId.
    List<IoTData> findByUserId(Long userId);

    // Pobieranie rekordów danych powiązanych z określonym userId w podanym zakresie czasu (od startDate do endDate).
    List<IoTData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Pobieranie określonego rekordu danych według jego id i userId.
    Optional<IoTData> findByIdAndUserId(Long id, Long userId);

    // Pobieranie wszystkich rekordów z bazy danych.
    List<IoTData> findAll();

    // Pobieranie rekordów danych dla określonego userId, w których timestamp znajduje się po podanym timestampie.
    List<IoTData> findByUserIdAndTimestampAfter(Long userId, LocalDateTime timestamp);

    /**
     * Aktualizacja lastRead dla rekordów danych powiązanych z określonym deviceId.
     * Ta operacja modyfikuje tabelę IoTData.
     */
    @Modifying
    @Transactional
    @Query("Update IoTData d SET d.lastRead =:timestamp WHERE d.deviceId = :deviceId")
    void updateLastRead(@Param("deviceId") String deviceId, @Param("timestamp")LocalDateTime timestamp);
}
