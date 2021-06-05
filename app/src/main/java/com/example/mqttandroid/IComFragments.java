package com.example.mqttandroid;

public interface IComFragments {
    void BtnClicked(int graph);
    void BtnClicked(int graph, int id);
    void SendRoom(Room room);
    void SendPerson(Room person);
    void SendMeasList(int graph, MeasList list);
    void RequestMeasurement(int id);
}
