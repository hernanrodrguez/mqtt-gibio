package com.example.mqttandroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataCO2 {
    private String hora;
    private String CO2;
    private String habitacion;
    private String ID;


    // tipo de medicion IDS           medicion        id          fecha-hora         daily-indice

    public DataCO2 (String hora,
                    String CO2, String habitacion, String ID) {
        this.hora = hora;
        this.CO2 = CO2;
        this.habitacion = habitacion;
        this.ID = ID; //ACA VA EL CODIGO PARA GENERAR UN ID AUTOINCREMENTAL
    }

    public String getCO2() {
        return (CO2);
    }

    public String getID() {
        return (ID);
    }

    public String getHora() {
        return (hora);
    }

    public String getHabitacion() {
        return (habitacion);
    }
}


