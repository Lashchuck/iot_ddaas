package com.iot_ddaas;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity

public class IoTData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String deviceId;
    private Integer sensor1;
    private Integer sensor2;
    private LocalDateTime timestamp;
    private Long userId;
    private Float temperatureSensor;
    private LocalDateTime lastRead;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        lastRead = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public IoTData(){}

    public IoTData(Long id, String deviceId, Integer sensor1, Integer sensor2, Long userId, Float temperatureSensor, LocalDateTime lastRead){

        this.id = id;
        this.deviceId = deviceId;
        this.sensor1 = sensor1;
        this.sensor2 = sensor2;
        this.userId = userId;
        this.temperatureSensor = temperatureSensor;
        this.lastRead = lastRead.truncatedTo(ChronoUnit.SECONDS);
    }

    public IoTData(String deviceId, Integer sensor1, Integer sensor2, Long userId, Float temperatureSensor, LocalDateTime lastRead) {
        this.deviceId = deviceId;
        this.sensor1 = sensor1;
        this.sensor2 = sensor2;
        this.userId = userId;
        this.temperatureSensor = temperatureSensor;
        this.lastRead = lastRead.truncatedTo(ChronoUnit.SECONDS);
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

    public Integer getSensor1() {
        return sensor1;
    }

    public void setSensor1(Integer sensor1) {
        this.sensor1 = sensor1;
    }

    public Integer getSensor2() {
        return sensor2;
    }

    public void setSensor2(Integer sensor2) {
        this.sensor2 = sensor2;
    }

    public Float getTemperatureSensor(){
        return temperatureSensor;
    }

    public void setTemperatureSensor(Float temperatureSensor){
        this.temperatureSensor = temperatureSensor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.truncatedTo(ChronoUnit.SECONDS);
    }

    public Long getUserId(){
        return userId;
    }

    public void setUserId(Long userId){
        this.userId = userId;
    }

    public LocalDateTime getLastRead() {
        return lastRead;
    }

    public void setLastRead(LocalDateTime lastRead) {
        this.lastRead = lastRead.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public String toString(){
        return "IoTData{" +
                "userId=" + userId +
                ", deviceId=" + deviceId +
                ", sensor1=" + sensor1 +
                ", sensor2=" + sensor2 +
                ", temperatureSensor=" + temperatureSensor +
                ", timestamp=" + timestamp +
                '}';
    }
}
