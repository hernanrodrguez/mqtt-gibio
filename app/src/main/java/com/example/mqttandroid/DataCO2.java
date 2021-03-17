package com.example.mqttandroid;

public class DataCO2 {
    private String hora;
    private String CO2;
    private String habitacion;

    public DataCO2 (String hora,
                    String CO2, String habitacion) {
        this.hora = hora;
        this.CO2 = CO2;
        this.habitacion = habitacion;
    }

    public String getCO2() {
        return (CO2);
    }

    public String getHora() {
        return (hora);
    }

    public String getHabitacion() {
        return (habitacion);
    }
}

