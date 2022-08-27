package com.example.mqttandroid;

public interface IComFragments {
    void btnClicked(int graph);
    void btnClicked(int graph, Dispositivo persona);
    void sendDispositivo(Dispositivo dispositivo);
    void sendArray(Dispositivo dispositivo, int graph);
    void sendPersona(Dispositivo person);
    void SendMeasList(int graph, ArrayMediciones list);
    void RequestMeasurement(int id);
    void clearCalibrationData();
}
