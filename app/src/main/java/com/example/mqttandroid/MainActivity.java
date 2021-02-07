package com.example.mqttandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Mqtt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service/
        
        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(),
                                                "tcp://test.mosquitto.org:1883",
                                                         clientId);

        try {
            Log.d(TAG, "Starting connection...");
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "Connected");

                    String topic = "test_gibio";
                    int qos = 1;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published
                                Log.d(TAG, "Subscribed");
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                                Log.d(TAG, "Failed to Subscribe");
                            }
                        });
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                                Log.d(TAG, "Connection Lost");
                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                String payload = new String(message.getPayload());
                                Log.d(TAG, "Message received: " + payload);
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                                Log.d(TAG, "Delivery Complete");
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    String topicc = "test_gibioo";
                    String payload = "Mensaje de Prueba";
                    byte[] encodedPayload;
                    try {
                        encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish(topicc, message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "Failed to connect");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }




}