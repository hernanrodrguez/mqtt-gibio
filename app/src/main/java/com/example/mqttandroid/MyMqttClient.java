package com.example.mqttandroid;

import android.content.Context;
import android.content.res.Resources;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

// https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
public class MyMqttClient {

    private final MqttAndroidClient client;
    private final MqttListener listener;

    private final String serverURI;
    private String topic;

    private boolean connected;
    private boolean subscribed;

    private final Resources res;

    public MyMqttClient(Context context, String URL){
        res = context.getResources();
        serverURI = URL;
        String clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, serverURI, clientID);
        listener = (MqttListener)context;
    }

    public void Subscribe(String subTopic){
        topic = subTopic;
        //ShowProgressDialog();
        if(!connected){
            Connect();
        } else {
            Subscribe();
        }
    }

    private void Connect(){
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connected = true;
                    Subscribe();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    listener.ConnectionFailed();
                    connected = false;
                }
            });
        } catch (MqttException e) {
            listener.ConnectionFailed();
            e.printStackTrace();
            connected = false;
        }
    }

    private void Subscribe(){
        try {
            IMqttToken token = client.subscribe(topic, 1);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    listener.BrokerAdded();
                    subscribed = true;
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    listener.ConnectionFailed();
                    subscribed = false;
                }
            });
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    listener.ConnectionLost();
                    subscribed = false;
                    connected = false;
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    listener.MessageArrived(topic, payload);
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });
        } catch (MqttException e) {
            listener.ConnectionFailed();
            e.printStackTrace();
            subscribed = false;
        }
    }

    public void Publish(String pubTopic, String payload){
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(pubTopic, message);
            listener.MessageSent();
        } catch (MqttException e) {
            e.printStackTrace();
            listener.MessageNotSent();
        }
    }

    public boolean IsConnected(){ return connected; }
    public boolean IsSubscribed(){ return subscribed; }

    public String GetTopic(){ return topic; }
    public String GetBaseTopic(){ return topic.split("/")[0]; }
    public String GetServerURL(){ return serverURI; }
    public String GetURL(){
        String URL = serverURI.split(":")[1];
        return URL.substring(2);
    }
    public String GetPort(){
        return serverURI.split(":")[2];
    }

    @Override
    public String toString() {
        return serverURI;
    }
}
