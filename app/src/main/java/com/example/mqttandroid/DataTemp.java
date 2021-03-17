package com.example.mqttandroid;

public class DataTemp {
    private String hora;
    private String temp;
    private String habitacion;

    public DataTemp (String hora,
                  String temp, String habitacion) {
        this.hora = hora;
        this.temp = temp;
        this.habitacion = habitacion;
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
}

