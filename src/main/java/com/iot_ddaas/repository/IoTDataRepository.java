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


@Repository
public interface IoTDataRepository extends JpaRepository<IoTData, Long> {

    List<IoTData> findByUserId(Long userId);
    List<IoTData> findByUserIdAndTimestampBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<IoTData> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Transactional
    @Query("Update IoTData d SET d.lastRead =:timestamp WHERE d.deviceId = :deviceId")
    void updateLastRead(@Param("deviceId") String deviceId, @Param("timestamp")LocalDateTime timestamp);
}
