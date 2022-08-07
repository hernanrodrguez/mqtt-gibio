package com.example.mqttandroid;

import java.io.Serializable;
import java.util.ArrayList;

public class ArrayMediciones implements Serializable {
    private int tipo_medicion;
    private int id_dispositivo;
    private String key_dispositivo;
    private ArrayList<Medicion> mediciones;

    public ArrayMediciones(){
        this.tipo_medicion = 0;
        this.mediciones = new ArrayList<>();
    }

    public ArrayMediciones(int tipo_medicion, int id_dispositivo) {
        this.tipo_medicion = tipo_medicion;
        this.mediciones = new ArrayList<>();
        this.id_dispositivo = id_dispositivo;
    }

    public ArrayMediciones(int tipo_medicion, int id_dispositivo, Medicion medicion) {
        this.tipo_medicion = tipo_medicion;
        this.id_dispositivo = id_dispositivo;
        this.mediciones = new ArrayList<>();
        this.mediciones.add(medicion);
    }

    public ArrayMediciones(int tipo_medicion, int id_dispositivo, ArrayList<Medicion> mediciones) {
        this.tipo_medicion = tipo_medicion;
        this.id_dispositivo = id_dispositivo;
        this.mediciones = mediciones;
    }

    public void setMeas(int tipo_medicion){ this.tipo_medicion = tipo_medicion; }
    public void setList(ArrayList<Medicion> mediciones){ this.mediciones = mediciones; }

    public int getTipoMedicion(){ return tipo_medicion; }
    public int getIdDispositivo(){ return id_dispositivo; }
    public String getKeyDispositivo(){ return key_dispositivo; }
    public ArrayList<Medicion> getMediciones(){ return mediciones; }

    public int size(){ return mediciones.size(); }
    public void addMedicion(Medicion medicion){ mediciones.add(medicion); }
    public void clearMediciones(){ mediciones = new ArrayList<>(); }

}
