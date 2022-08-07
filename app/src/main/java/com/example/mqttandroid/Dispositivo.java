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

    public Dispositivo(Dispositivo d) {
        this.id = d.getId();
        this.key = d.getKey();
        this.tipo_dispositivo = d.getTipoDispositivo();

        this.mediciones_temp_amb = d.getTAmbArray();
        this.mediciones_temp_obj = d.getTObjArray();
        this.mediciones_co2 = d.getCO2Array();
        this.mediciones_spo2 = d.getSpo2Array();
        this.mediciones_hr = d.getHRArray();
    }

    public Dispositivo(int id, String key, int tipo_dispositivo) {
        this.id = id;
        this.key = key;
        this.tipo_dispositivo = tipo_dispositivo;

        this.mediciones_temp_amb = new ArrayMediciones(Constants.TEMPERATURA_AMBIENTE, id);
        this.mediciones_temp_obj = new ArrayMediciones(Constants.TEMPERATURA_SUJETO, id);
        this.mediciones_co2 = new ArrayMediciones(Constants.CO2, id);
        this.mediciones_spo2 = new ArrayMediciones(Constants.SPO2, id);
        this.mediciones_hr = new ArrayMediciones(Constants.FRECUENCIA_CARDIACA, id);
    }

    public void addMedicion(Medicion medicion, int tipo_medicion){
        switch (tipo_medicion) {
            case Constants.TEMPERATURA_SUJETO:
                mediciones_temp_obj.addMedicion(medicion);
                break;
            case Constants.TEMPERATURA_AMBIENTE:
                mediciones_temp_amb.addMedicion(medicion);
                break;
            case Constants.CO2:
                mediciones_co2.addMedicion(medicion);
                break;
            case Constants.SPO2:
                mediciones_spo2.addMedicion(medicion);
                break;
            case Constants.FRECUENCIA_CARDIACA:
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
            case Constants.TEMPERATURA_SUJETO:
                return mediciones_temp_obj.size();
            case Constants.TEMPERATURA_AMBIENTE:
                return mediciones_temp_amb.size();
            case Constants.CO2:
                return mediciones_co2.size();
            case Constants.SPO2:
                return mediciones_spo2.size();
            case Constants.FRECUENCIA_CARDIACA:
                return mediciones_hr.size();
            default:
                return -1;
        }
    }

    public Medicion getUltimaMedicion(int id_meas){
        ArrayList<Medicion> list;
        switch (id_meas){
            case Constants.TEMPERATURA_SUJETO:
                list = mediciones_temp_obj.getMediciones();
                return list.get(list.size()-1);
            case Constants.TEMPERATURA_AMBIENTE:
                list = mediciones_temp_amb.getMediciones();
                return list.get(list.size()-1);
            case Constants.CO2:
                list = mediciones_co2.getMediciones();
                return list.get(list.size()-1);
            case Constants.SPO2:
                list = mediciones_spo2.getMediciones();
                return list.get(list.size()-1);
            case Constants.FRECUENCIA_CARDIACA:
                list = mediciones_hr.getMediciones();
                return list.get(list.size()-1);
            default:
                return null;
        }
    }

    public ArrayMediciones getArray(int id_meas){
        switch (id_meas){
            case Constants.TEMPERATURA_SUJETO:
                return mediciones_temp_obj;
            case Constants.TEMPERATURA_AMBIENTE:
                return mediciones_temp_amb;
            case Constants.CO2:
                return mediciones_co2;
            case Constants.SPO2:
                return mediciones_spo2;
            case Constants.FRECUENCIA_CARDIACA:
                return mediciones_hr;
            default:
                return new ArrayMediciones();
        }
    }

    @Override
    public String toString() {
        return ("[DISPOSITIVO] Key: " + this.key + " Tipo dispositivo: " + this.tipo_dispositivo + " ID: " + this.id);
    }
}
