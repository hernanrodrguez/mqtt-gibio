package com.example.mqttandroid;

import java.io.Serializable;
import java.util.ArrayList;

public class Room implements Serializable {
    private final String id_room;

    private final MeasList tamb_meas;
    private final MeasList tobj_meas;
    private final MeasList co2_meas;
    private final MeasList spo2_meas;

    public Room() throws Exception {
        this.id_room = "";

        this.tamb_meas = new MeasList(Constants.TEMP_AMB_ID, id_room);
        this.tobj_meas = new MeasList(Constants.TEMP_OBJ_ID, id_room);
        this.co2_meas = new MeasList(Constants.CO2_ID, id_room);
        this.spo2_meas = new MeasList(Constants.SPO2_ID, id_room);
    }

    public Room(String id_room) throws Exception {
        this.id_room = id_room;

        this.tamb_meas = new MeasList(Constants.TEMP_AMB_ID, id_room);
        this.tobj_meas = new MeasList(Constants.TEMP_OBJ_ID, id_room);
        this.co2_meas = new MeasList(Constants.CO2_ID, id_room);
        this.spo2_meas = new MeasList(Constants.SPO2_ID, id_room);

    }

    public void Add(Measurement measurement, int id_meas){
        switch (id_meas) {
            case Constants.TEMP_OBJ_ID:
                tobj_meas.Add(measurement);
                break;
            case Constants.TEMP_AMB_ID:
                tamb_meas.Add(measurement);
                break;
            case Constants.CO2_ID:
                co2_meas.Add(measurement);
                break;
            case Constants.SPO2_ID:
                spo2_meas.Add(measurement);
                break;
            default:
                break;
        }
    }

    public void AddTAmb(Measurement measurement){
        tamb_meas.Add(measurement);
    }

    public void AddTObj(Measurement measurement){
        tobj_meas.Add(measurement);
    }

    public void AddCo2(Measurement measurement){
        co2_meas.Add(measurement);
    }

    public void AddSpo2(Measurement measurement){
        spo2_meas.Add(measurement);
    }

    public String GetIdRoom(){ return id_room; }
    public MeasList GetTAmbList(){ return tamb_meas; }
    public MeasList GetTObjList(){ return tobj_meas; }
    public MeasList GetCo2List(){ return co2_meas; }
    public MeasList GetSpo2List(){ return spo2_meas; }

    public int GetLastIndex(int id_meas){
        switch (id_meas){
            case Constants.TEMP_OBJ_ID:
                return tobj_meas.Size();
            case Constants.TEMP_AMB_ID:
                return tamb_meas.Size();
            case Constants.CO2_ID:
                return co2_meas.Size();
            case Constants.SPO2_ID:
                return spo2_meas.Size();
            default:
                return -1;
        }
    }

    public Measurement GetLastMeasurement(int id_meas){
        ArrayList<Measurement> list;
        switch (id_meas){
            case Constants.TEMP_OBJ_ID:
                list = tobj_meas.GetList();
                return list.get(list.size()-1);
            case Constants.TEMP_AMB_ID:
                list = tamb_meas.GetList();
                return list.get(list.size()-1);
            case Constants.CO2_ID:
                list = co2_meas.GetList();
                return list.get(list.size()-1);
            case Constants.SPO2_ID:
                list = spo2_meas.GetList();
                return list.get(list.size()-1);
            default:
                return null;
        }
    }

    public MeasList GetList(int id_meas){
        switch (id_meas){
            case Constants.TEMP_OBJ_ID:
                return tobj_meas;
            case Constants.TEMP_AMB_ID:
                return tamb_meas;
            case Constants.CO2_ID:
                return co2_meas;
            case Constants.SPO2_ID:
                return spo2_meas;
            default:
                return null;
        }
    }
}
