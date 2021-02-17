package com.example.mqttandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttListener{

    private final String TAG = "Main Activity";
    private final String PROTOCOL = "tcp://";

    private MyMqttClient mqttClient;

    private EditText etAddress;
    private EditText etPort;
    private EditText etTopic;
    private EditText etMessage;
    private EditText etTopicSend;

    private List<String> messages;
    private ArrayAdapter<String> adapter;

    private int currentView;

    private final static String BROKER_KEY = "Broker";
    private final static String TOPIC_KEY = "Topic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = new ArrayList<>();
        GetBroker();
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

    private boolean GetBroker(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String URL = prefs.getString(BROKER_KEY, "");
        String topic = prefs.getString(TOPIC_KEY, "");
        if(!URL.equals("") && !topic.equals("")){
            mqttClient = new MyMqttClient(this, URL);
            mqttClient.Subscribe(topic);
            return true;
        } else
            return false;
    }

    private void SaveBroker(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BROKER_KEY, mqttClient.GetURL());
        editor.putString(TOPIC_KEY, mqttClient.GetTopic());
        editor.apply();
    }

    private void SetUpMainActivity(){
        currentView = R.layout.activity_main;
        setContentView(currentView);

        Button btnAddNewBroker = findViewById(R.id.btnAddNewBroker);
        btnAddNewBroker.setOnClickListener(this::btnAddNewBrokerClick);
        Button btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(this::btnSendMessageClick);

        ListView lvMsg = findViewById(R.id.lvMsg);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
        adapter.notifyDataSetChanged();
        lvMsg.setAdapter(adapter);
    }

    private void btnSendMessageClick(View view) {
        if(mqttClient == null){
            Toast.makeText(this, R.string.err_broker, Toast.LENGTH_SHORT).show();
        } else {
            SetUpSendMessageActivity();
        }
    }

    private void SetUpSendMessageActivity() {
        currentView = R.layout.activity_send_message;
        setContentView(currentView);

        etMessage = findViewById(R.id.etMsg);
        etMessage.setText(R.string.lbl_test_message);
        etTopicSend = findViewById(R.id.etTopicSend);
        etTopicSend.setText(R.string.lbl_test_topic_send);

        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this::btnSendClick);
    }

    private void btnSendClick(View view) {
        ArrayList<EditText> arr = new ArrayList<EditText>(){
            {
                add(etTopicSend);
                add(etMessage);
            }
        };


        if(CheckFields(arr)){
            Editable topic = etTopicSend.getText();
            Editable payload = etMessage.getText();

            mqttClient.Publish(String.valueOf(topic), String.valueOf(payload));
            SetUpMainActivity();
        }
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
        ArrayList<EditText> arr = new ArrayList<EditText>(){
            {
                add(etAddress);
                add(etTopic);
                add(etPort);
            }
        };

        if(CheckFields(arr)){
            Editable address = etAddress.getText();
            Editable topic = etTopic.getText();
            Editable port = etPort.getText();

            mqttClient = new MyMqttClient(this, PROTOCOL + address + ":" + port);
            mqttClient.Subscribe(String.valueOf(topic));

            SaveBroker();
            SetUpMainActivity();
        }
    }

    private boolean CheckFields(List<EditText> editTexts){
        boolean ret = true;
        String err = getResources().getString(R.string.err_field);

        for(int i=0; i<editTexts.size(); i++){
            if(TextUtils.isEmpty(editTexts.get(i).getText())){
                editTexts.get(i).setError(err);
                ret = false;
            }
        }
        return ret;
    }

    @Override
    public void MessageArrived(String msg) {
        messages.add(msg);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void MessageSent() {
        Toast.makeText(this, R.string.lbl_msg_sent, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void MessageNotSent() {
        Toast.makeText(this, R.string.err_send, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void BrokerAdded() {
        Toast.makeText(this, R.string.lbl_connected, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ConnectionFailed() {
        Toast.makeText(this, R.string.err_connect, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ConnectionLost() {
        Toast.makeText(this, R.string.lbl_conn_lost, Toast.LENGTH_SHORT).show();
    }
}