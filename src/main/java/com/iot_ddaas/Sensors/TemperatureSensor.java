package com.iot_ddaas.Sensors;
import com.iot_ddaas.Anomaly;

public  class TemperatureSensor extends Sensor{

    private Float temperature;

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    @Override
    public boolean detectAnomaly(Number threshold){

        if (temperature == null){
            return true; // true, ponieważ brak odczytu jest anomalią
        }

        return temperature > threshold.floatValue();
    }


    public Anomaly createAnomaly(String description) {

        Anomaly anomaly = new Anomaly();
        anomaly.setDeviceId(this.getDeviceId());
        anomaly.setType(description);
        anomaly.setWartosc(this.getTemperature() != null ? this.getTemperature().intValue() : null); // Konwersja Float do Integer
        anomaly.setTimestamp(this.getTimestamp());
        anomaly.setUserId(this.getUserId());
        return anomaly;
    }
}
