package com.iot_ddaas;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String deviceId;
    private String type;
    private Integer wartosc;
    private LocalDateTime timestamp;
    private Long userId;


    public Anomaly(){}

    public Anomaly(Long id, String deviceId, LocalDateTime timestamp, String type, Long userId, Integer wartosc){

        this.id = id;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.type = type;
        this.userId = userId;
        this.wartosc = wartosc;
    }


    // Gettery i settery
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWartosc() {
        return wartosc;
    }

    public void setWartosc(Integer wartosc) {
        this.wartosc = wartosc;
    }

    public LocalDateTime getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp){
        this.timestamp=timestamp;
    }

    public Long getUserId(){
        return userId;
    }

    public void setUserId(Long userId){
        this.userId = userId;
    }
}
