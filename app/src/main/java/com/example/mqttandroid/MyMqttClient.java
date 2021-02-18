package com.example.mqttandroid;

import android.app.ProgressDialog;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.loader.ResourcesProvider;
import android.os.AsyncTask;
import android.util.EventLog;
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

    private final MqttAndroidClient client;
    private final MqttListener listener;

    private final String serverURI;
    private String topic;

    private boolean connected;
    private boolean subscribed;

    private final Resources res;

    private final ProgressDialog progressDialog;

    public MyMqttClient(Context context, String URL){
        res = context.getResources();
        serverURI = URL;
        String clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, serverURI, clientID);
        progressDialog = new ProgressDialog(context);
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
                    HideProgressDialog();
                }
            });
        } catch (MqttException e) {
            listener.ConnectionFailed();
            e.printStackTrace();
            connected = false;
            HideProgressDialog();
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
                    HideProgressDialog();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    listener.ConnectionFailed();
                    subscribed = false;
                    HideProgressDialog();
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
                    listener.MessageArrived(topic + ": " + payload);
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });
        } catch (MqttException e) {
            listener.ConnectionFailed();
            e.printStackTrace();
            subscribed = false;
            HideProgressDialog();
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

    private void ShowProgressDialog(){
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(res.getString(R.string.lbl_loading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void HideProgressDialog(){
        progressDialog.cancel();
    }

    public boolean IsConnected(){ return connected; }
    public boolean IsSubscribed(){ return subscribed; }

    public String GetTopic(){ return topic; }
    public String GetURL(){ return serverURI; }

    @Override
    public String toString() {
        return serverURI;
    }
}
