package com.iot_ddaas.sensors;
import com.iot_ddaas.Anomaly;

import java.time.LocalDateTime;

public abstract class Sensor {

    protected String deviceId;
    protected LocalDateTime timestamp;
    protected Long userId;

    public Anomaly createAnomaly(String type){
        Anomaly anomaly = new Anomaly();
        anomaly.setDeviceId(this.deviceId);
        anomaly.setType(type);
        anomaly.setTimestamp(LocalDateTime.now());
        return anomaly;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public abstract boolean detectAnomaly(Number threshold);
}
