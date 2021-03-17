package com.example.mqttandroid;

public class DataTemp {
    private String hora;
    private String temp;
    private String habitacion;
    private String ID;

    public DataTemp (String hora,
                  String temp, String habitacion, String ID) {
        this.hora = hora;
        this.temp = temp;
        this.habitacion = habitacion;
        this.ID = ID;
    }

    public String getTemp() {
        return temp;
    }

    public String getHora() {
        return hora;
    }

    public String getHabitacion() {
        return habitacion;
    }

    public String getID() {
        return (ID);
    }
}

