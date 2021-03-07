package com.example.mqttandroid;

import java.io.Serializable;
import java.util.Date;

public class Measurement implements Serializable {
    private final double value;
    private Date date;
    private int sample;

    public Measurement(double value, Date date){
        this.value = value;
        this.date = date;
    }

    public Measurement(double value, int sample){
        this.value = value;
        this.sample = sample;
    }

    public double GetValue(){ return value; }
    public Date GetDate(){ return date; }
    public int GetSample(){ return sample; }

}
