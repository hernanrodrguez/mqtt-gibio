package com.example.mqttandroid;

public class Room {
    private String id_room;

    private MeasList tamb_meas;
    private MeasList tobj_meas;
    private MeasList co2_meas;
    private MeasList spo2_meas;

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


}
