package com.example.mqttandroid;

public interface MqttListener {
    void MessageArrived(String topic, String msg);
    void MessageSent();
    void MessageNotSent();
    void BrokerAdded();
    void ConnectionFailed();
    void ConnectionLost();
}
