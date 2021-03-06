package com.example.mqttandroid;

public interface IComData {
    void DataArrived(Double value, int key);
    void MeasArrived(String id_room, int id_meas, Measurement measurement);
}
