package com.example.mqttandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
public class MyMqttClient {

    private final String TAG = "MyMqttClient";

    private MqttAndroidClient client;

    private String clientID;
    private String serverURI;
    private String topic;

    private List<String> messages;

    private boolean connected;
    private boolean subscribed;

    private Context context;

    public MyMqttClient(Context context, String URL){
        this.context = context;
        messages = new ArrayList<>();
        serverURI = URL;
        clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, serverURI, clientID);
    }

    public void Subscribe(String subTopic){
        topic = subTopic;
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
                    Log.d(TAG, "Connected");
                    connected = true;
                    Subscribe();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Not Connected");
                    connected = false;
                }
            });
        } catch (MqttException e) {
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
                    Toast.makeText(context, "Suscripto a: " + topic, Toast.LENGTH_SHORT).show();
                    subscribed = true;
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(context, "Failed to Subscribe", Toast.LENGTH_SHORT).show();
                    subscribed = false;
                }
            });
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(context, "Connection Lost", Toast.LENGTH_SHORT).show();
                    subscribed = false;
                    connected = false;
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    messages.add(payload);
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Delivery Complete");
                }
            });
        } catch (MqttException e) {
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
            Toast.makeText(context, "Mensaje Enviado!", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean IsConnected(){ return connected; }
    public boolean IsSubscribed(){ return subscribed; }
    public List<String> GetMessages(){ return messages; }
    public String GetTopic(){ return topic; }

}
