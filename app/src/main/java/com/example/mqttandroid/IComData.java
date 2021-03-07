package com.example.mqttandroid;

public interface IComData {
    void MeasArrived(String id_room, int id_meas, Measurement measurement);
}
