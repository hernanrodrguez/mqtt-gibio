package com.example.mqttandroid;

import java.io.Serializable;
import java.util.Date;

public class Medicion implements Serializable, Comparable<Medicion> {
    private final double value;
    private Date date;
    private int sample;

    public Medicion(double value, Date date){
        this.value = value;
        this.date = date;
    }

    public Medicion(double value, int sample){
        this.value = value;
        this.sample = sample;
    }

    public double getValue(){ return value; }
    public Date getDate(){ return date; }
    public int getSample(){ return sample; }

    @Override
    public String toString() {
        return ("[MEDICION] Valor: " + this.value + " Fecha: " + this.date);
    }

    @Override
    public int compareTo(Medicion o) {
        return date.compareTo(o.getDate());
    }
}
