package com.example.mqttandroid;

import android.icu.util.EthiopicCalendar;
import android.telecom.StatusHints;

import java.io.Serializable;
import java.util.ArrayList;

public class MeasList implements Serializable {
    private int id_meas;
    private String id_room;
    private ArrayList<Measurement> measurements;

    public MeasList(){
        this.id_meas = 0;
        this.measurements = new ArrayList<>();
    }

    public MeasList(int id_meas, String id_room) throws Exception {
        if(Constants.CheckIdMeas(id_meas))
            this.id_meas = id_meas;
        else
            throw new Exception("Invalid id_meas");
        this.measurements = new ArrayList<>();
        this.id_room = id_room;
    }

    public MeasList(int id_meas, String id_room, Measurement measurement) throws Exception {
        if(Constants.CheckIdMeas(id_meas))
            this.id_meas = id_meas;
        else
            throw new Exception("Invalid id_meas");
        this.id_room = id_room;
        this.measurements = new ArrayList<>();
        this.measurements.add(measurement);
    }

    public MeasList(int id_meas, String id_room, ArrayList<Measurement> measurements) throws Exception {
        if(Constants.CheckIdMeas(id_meas))
            this.id_meas = id_meas;
        else
            throw new Exception("Invalid id_meas");
        this.id_room = id_room;
        this.measurements = measurements;
    }

    public void SetMeas(int id_meas){ this.id_meas = id_meas; }
    public void SetList(ArrayList<Measurement> measurements){ this.measurements = measurements; }

    public int GetMeas(){ return id_meas; }
    public String GetRoom(){ return id_room; }
    public ArrayList<Measurement> GetList(){ return measurements; }

    public int Size(){ return measurements.size(); }
    public void Add(Measurement measurement){ measurements.add(measurement); }
    public void ClearList(){ measurements = new ArrayList<>(); }

}
