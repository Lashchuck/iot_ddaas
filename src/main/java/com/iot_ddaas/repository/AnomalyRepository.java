package com.iot_ddaas.repository;

import com.iot_ddaas.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {

    List<Anomaly> findByUserId(Long userId);
    Optional<Anomaly> findByIdAndUserId(Long id, Long userId);
    Optional<Anomaly> findTopByDeviceIdOrderByTimestampDesc(String deviceId);

    boolean existsByTimestamp(LocalDateTime timestamp);


}
