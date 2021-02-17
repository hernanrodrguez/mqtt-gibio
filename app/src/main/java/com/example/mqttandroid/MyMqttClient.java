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

    private final String TAG = "MyMqttClient";

    private MqttAndroidClient client;
    private MqttListener listener;

    private String clientID;
    private String serverURI;
    private String topic;

    private boolean connected;
    private boolean subscribed;

    private final Context context;
    private final Resources res;

    private final ProgressDialog progressDialog;

    public MyMqttClient(Context context, String URL){
        this.context = context;
        res = context.getResources();
        serverURI = URL;
        clientID = MqttClient.generateClientId();
        client = new MqttAndroidClient(context, serverURI, clientID);
        progressDialog = new ProgressDialog(context);
        listener = (MqttListener)context;
    }

    public void Subscribe(String subTopic){
        topic = subTopic;
        ShowProgressDialog();
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
                    Toast.makeText(context, R.string.err_connect, Toast.LENGTH_SHORT).show();
                    connected = false;
                    HideProgressDialog();
                }
            });
        } catch (MqttException e) {
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
                    //Toast.makeText(context, res.getString(R.string.lbl_connected), Toast.LENGTH_SHORT).show();
                    listener.BrokerAdded();
                    subscribed = true;
                    HideProgressDialog();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(context, R.string.lbl_sub_failed, Toast.LENGTH_SHORT).show();
                    subscribed = false;
                    HideProgressDialog();
                }
            });
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Toast.makeText(context, R.string.lbl_conn_lost, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, R.string.lbl_msg_sent, Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.err_send, Toast.LENGTH_SHORT).show();
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
