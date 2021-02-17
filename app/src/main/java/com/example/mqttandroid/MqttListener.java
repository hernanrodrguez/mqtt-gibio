package com.example.mqttandroid;

public interface MqttListener {
    void MessageArrived(String msg);
    void MessageSent();
    void MessageNotSent();
    void BrokerAdded();
    void ConnectionFailed();
    void ConnectionLost();
}
