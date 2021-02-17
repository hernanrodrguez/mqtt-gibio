package com.example.mqttandroid;

public interface MqttListener {
    void MessageArrived(String msg);
    void BrokerAdded();
}
