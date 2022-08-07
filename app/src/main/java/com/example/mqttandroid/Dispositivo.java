package com.example.mqttandroid;

import java.io.Serializable;
import java.util.ArrayList;

public class Dispositivo implements Serializable {
    private final String key;
    private final int id;
    private final int tipo_dispositivo;

    private final ArrayMediciones mediciones_temp_amb;
    private final ArrayMediciones mediciones_temp_obj;
    private final ArrayMediciones mediciones_co2;
    private final ArrayMediciones mediciones_spo2;
    private final ArrayMediciones mediciones_hr;

    public Dispositivo(int id, String key, int tipo_dispositivo) {
        this.id = id;
        this.key = key;
        this.tipo_dispositivo = tipo_dispositivo;

        this.mediciones_temp_amb = new ArrayMediciones(Constants.TEMP_AMB_ID, id);
        this.mediciones_temp_obj = new ArrayMediciones(Constants.TEMP_OBJ_ID, id);
        this.mediciones_co2 = new ArrayMediciones(Constants.CO2_ID, id);
        this.mediciones_spo2 = new ArrayMediciones(Constants.SPO2_ID, id);
        this.mediciones_hr = new ArrayMediciones(Constants.HR_ID, id);
    }

    public void addMedicion(Medicion medicion, int tipo_medicion){
        switch (tipo_medicion) {
            case Constants.TEMP_OBJ_ID:
                mediciones_temp_obj.addMedicion(medicion);
                break;
            case Constants.TEMP_AMB_ID:
                mediciones_temp_amb.addMedicion(medicion);
                break;
            case Constants.CO2_ID:
                mediciones_co2.addMedicion(medicion);
                break;
            case Constants.SPO2_ID:
                mediciones_spo2.addMedicion(medicion);
                break;
            case Constants.HR_ID:
                mediciones_hr.addMedicion(medicion);
                break;
            default:
                break;
        }
    }

    public void AddTAmb(Medicion medicion){
        mediciones_temp_obj.addMedicion(medicion);
    }

    public void AddTObj(Medicion medicion){
        mediciones_temp_amb.addMedicion(medicion);
    }

    public void AddCo2(Medicion medicion){
        mediciones_co2.addMedicion(medicion);
    }

    public void AddSpo2(Medicion medicion){
        mediciones_spo2.addMedicion(medicion);
    }

    public void AddHR(Medicion medicion){ mediciones_hr.addMedicion(medicion); }

    public int getId(){ return id; }
    public String getKey(){ return key; }
    public int getTipoDispositivo(){ return tipo_dispositivo; }
    public ArrayMediciones getTAmbArray(){ return mediciones_temp_amb; }
    public ArrayMediciones getTObjArray(){ return mediciones_temp_obj; }
    public ArrayMediciones getCO2Array(){ return mediciones_co2; }
    public ArrayMediciones getSpo2Array(){ return mediciones_spo2; }
    public ArrayMediciones getHRArray(){ return mediciones_hr; }

    public int getUltimoIndice(int tipo_medicion){
        switch (tipo_medicion){
            case Constants.TEMP_OBJ_ID:
                return mediciones_temp_obj.size();
            case Constants.TEMP_AMB_ID:
                return mediciones_temp_amb.size();
            case Constants.CO2_ID:
                return mediciones_co2.size();
            case Constants.SPO2_ID:
                return mediciones_spo2.size();
            case Constants.HR_ID:
                return mediciones_hr.size();
            default:
                return -1;
        }
    }

    public Medicion getUltimaMedicion(int id_meas){
        ArrayList<Medicion> list;
        switch (id_meas){
            case Constants.TEMP_OBJ_ID:
                list = mediciones_temp_obj.getMediciones();
                return list.get(list.size()-1);
            case Constants.TEMP_AMB_ID:
                list = mediciones_temp_amb.getMediciones();
                return list.get(list.size()-1);
            case Constants.CO2_ID:
                list = mediciones_co2.getMediciones();
                return list.get(list.size()-1);
            case Constants.SPO2_ID:
                list = mediciones_spo2.getMediciones();
                return list.get(list.size()-1);
            case Constants.HR_ID:
                list = mediciones_hr.getMediciones();
                return list.get(list.size()-1);
            default:
                return null;
        }
    }

    public ArrayMediciones getArray(int id_meas){
        switch (id_meas){
            case Constants.TEMP_OBJ_ID:
                return mediciones_temp_obj;
            case Constants.TEMP_AMB_ID:
                return mediciones_temp_amb;
            case Constants.CO2_ID:
                return mediciones_co2;
            case Constants.SPO2_ID:
                return mediciones_spo2;
            case Constants.HR_ID:
                return mediciones_hr;
            default:
                return new ArrayMediciones();
        }
    }
}
