package com.example.mqttandroid;

public interface IComFragments {
    void BtnClicked(int graph);
    void BtnClicked(int graph, int id);
    void sendDispositivo(Dispositivo dispositivo);
    void SendPerson(Dispositivo person);
    void SendMeasList(int graph, ArrayMediciones list);
    void RequestMeasurement(int id);
    void clearCalibrationData();
}
