package com.example.mqttandroid;

import java.util.UUID;

public class Rooms {
    private Long TimeMS;
    private Double Measurement;
    private Integer ID;

    public Rooms (Long TimeMS,
                  Double Measurement) {
        this.TimeMS = System.currentTimeMillis();
        this.Measurement = Measurement;
    }

    public Double getTemp() {
        return Measurement;
    }

    public Long getHora() {
        return TimeMS;
    }

    public Integer getID() {
        return (ID);
    }
}

