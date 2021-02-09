package com.example.mqttandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Main Activity";
    private final String PROTOCOL = "tcp://";

    private MyMqttClient mqttClient;

    private EditText etAddress;
    private EditText etPort;
    private EditText etTopic;
    private EditText etMessage;
    private EditText etTopicSend;

    private int currentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetUpMainActivity();
    }

    @Override
    public void onBackPressed() {
        switch (currentView){
            case R.layout.activity_main:
                finish();
                break;
            case R.layout.activity_add_new_broker:
            case R.layout.activity_send_message:
                SetUpMainActivity();
                break;
            default:
                finish();
                break;
        }
    }

    private void SetUpMainActivity(){
        currentView = R.layout.activity_main;
        setContentView(currentView);

        Button btnAddNewBroker = findViewById(R.id.btnAddNewBroker);
        btnAddNewBroker.setOnClickListener(this::btnAddNewBrokerClick);
        Button btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(this::btnSendMessageClick);

        ListView lvMsg = findViewById(R.id.lvMsg);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        if(mqttClient != null){
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mqttClient.GetMessages());
        }
        adapter.notifyDataSetChanged();
        lvMsg.setAdapter(adapter);
    }

    private void btnSendMessageClick(View view) {
        SetUpSendMessageActivity();
    }

    private void SetUpSendMessageActivity() {
        currentView = R.layout.activity_send_message;
        setContentView(currentView);

        etMessage = findViewById(R.id.etMsg);
        etMessage.setText(R.string.lbl_test_message);
        etTopicSend = findViewById(R.id.etTopicSend);
        etTopicSend.setText(R.string.lbl_test_topic);

        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this::btnSendClick);
    }

    private void btnSendClick(View view) {
        String topic = String.valueOf(etTopicSend.getText());
        String payload = String.valueOf(etMessage.getText());
        mqttClient.Publish(topic, payload);
        SetUpMainActivity();
    }

    private void btnAddNewBrokerClick(View view) {
        SetUpAddNewBrokerActivity();
    }

    private void SetUpAddNewBrokerActivity() {
        currentView = R.layout.activity_add_new_broker;
        setContentView(currentView);

        etAddress = findViewById(R.id.etAddress);
        etPort = findViewById(R.id.etPort);
        etTopic = findViewById(R.id.etTopic);

        etAddress.setText(R.string.lbl_test_address);
        etPort.setText(R.string.lbl_test_port);
        etTopic.setText(R.string.lbl_test_topic);

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this::btnAddClick);
    }

    private void btnAddClick(View view) {
        Editable address = etAddress.getText();
        Editable topic = etTopic.getText();
        Editable port = etPort.getText();

        mqttClient = new MyMqttClient(this, PROTOCOL + address + ":" + port);
        mqttClient.Subscribe(String.valueOf(topic));
        SetUpMainActivity();
    }
}