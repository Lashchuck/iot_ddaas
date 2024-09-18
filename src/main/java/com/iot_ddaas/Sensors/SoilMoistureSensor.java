package com.iot_ddaas.Sensors;
import com.iot_ddaas.Anomaly;


public class SoilMoistureSensor extends Sensor {

    private Integer moistureValue;

    @Override
    public boolean detectAnomaly(Number threshold){
        return moistureValue < threshold.intValue();
    }

    public Integer getMoistureValue(){
        return moistureValue;
    }

    public void setMoistureValue(Integer moistureValue){
        this.moistureValue = moistureValue;
    }

    public Anomaly createAnomaly(String description){

        Anomaly anomaly = new Anomaly();
        anomaly.setDeviceId(this.getDeviceId());
        anomaly.setType(description);
        anomaly.setWartosc(this.getMoistureValue());
        anomaly.setTimestamp(this.getTimestamp());
        anomaly.setUserId(this.getUserId());
        return anomaly;
    }
}
