package com.example.mqttandroid;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttListener, IComFragments{

    private MyMqttClient mqttClient;

    private EditText etAddress;
    private EditText etPort;
    private EditText etTopic;
    private EditText etMessage;
    private EditText etTopicSend;

    private ListView lvMsg;
    private List<String> messages;
    private ArrayAdapter<String> adapter;

    private int currentView;


    private HomeFragment homeFragment;
    private PlotFragment plotFragment;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Menu menu;
    private TextView textView;

    private CheckBox cbRemember;
    private TextView btnEnter;

    private ProgressDialog progressDialog;


    private final static String BROKER_KEY = "Broker";
    private final static String TOPIC_KEY = "Topic";
    private final static String DATA_KEY = "Data";
    private final static String CASE_KEY = "Case";

    private final static String TEMP_OBJ_KEY = "TO";
    private final static String TEMP_AMB_KEY = "TA";
    private final static String CO2_KEY = "C";
    private final static String SPO2_KEY = "S";

    private final static int TEMP_OBJ = 1;
    private final static int TEMP_AMB = 2;
    private final static int CO2 = 3;
    private final static int SPO2 = 4;


    private ArrayList<Double> temp_obj_list;
    private ArrayList<Double> temp_amb_list;
    private ArrayList<Double> co2_list;
    private ArrayList<Double> spo2_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SetUpSplashScreen();
        messages = new ArrayList<>();
        progressDialog = new ProgressDialog(this);

        temp_obj_list = new ArrayList<>();
        temp_amb_list = new ArrayList<>();
        co2_list = new ArrayList<>();
        spo2_list = new ArrayList<>();

        if(!GetBroker())
            SetUpInitActivity();

        //SetUpMainActivity();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            switch (currentView){
                case R.layout.activity_settings:
                case R.layout.activity_send_message:
                    SetUpHomeActivity();
                    break;
                default:
                    if(getSupportFragmentManager().findFragmentById(R.id.fragHome) instanceof PlotFragment)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, homeFragment).commit();
                    else
                        finish();
                    break;
            }
        }
    }

    private boolean GetBroker(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // CLEAR PREFERENCES
        //SharedPreferences.Editor editor = prefs.edit();
        //editor.clear();
        //editor.apply();

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
        editor.putString(BROKER_KEY, mqttClient.GetServerURL());
        editor.putString(TOPIC_KEY, mqttClient.GetTopic());
        editor.apply();
    }

    private void SetUpSplashScreen(){
        currentView = R.layout.splash_screen;
        setContentView(currentView);
    }

    private void SetUpMainActivity(){
        currentView = R.layout.activity_main;
        setContentView(currentView);

        Button btnAddNewBroker = findViewById(R.id.btnAddNewBroker);
        btnAddNewBroker.setOnClickListener(this::btnAddNewBrokerClick);
        Button btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(this::btnSendMessageClick);

        lvMsg = findViewById(R.id.lvMsg);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
        adapter.notifyDataSetChanged();
        lvMsg.setAdapter(adapter);
    }

    private void SetUpHomeActivity(){
        currentView = R.layout.activity_home;
        setContentView(currentView);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        textView = findViewById(R.id.textView);
        toolbar = findViewById(R.id.toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.lbl_nav_open, R.string.lbl_nav_closed);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this::NavigationItemSelected);
        navigationView.setCheckedItem(R.id.nav_home);

        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, homeFragment).commit();
    }

    private boolean NavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                SetUpHomeActivity();
                break;
            case R.id.nav_room:
                SendData(TEMP_AMB);
                break;
            case R.id.nav_person:
                SendData(TEMP_OBJ);
                break;
            case R.id.nav_co2:
                SendData(CO2);
                break;
            case R.id.nav_rooms:
                SendData(0);
                break;
            case R.id.nav_settings:
                SetUpSettingsActivity();
                break;
            case R.id.nav_info:
                break;
            case R.id.nav_bug:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void SetUpInitActivity(){
        currentView = R.layout.activity_init;
        setContentView(currentView);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        etAddress = findViewById(R.id.etAddressInit);
        etPort = findViewById(R.id.etPortInit);
        etTopic = findViewById(R.id.etTopicInit);
        cbRemember = findViewById(R.id.cbRemember);
        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this::BtnEnterClick);
    }

    private void BtnEnterClick(View view) {
        ArrayList<EditText> arr = new ArrayList<EditText>(){
            {
                add(etAddress);
                add(etPort);
                add(etTopic);
            }
        };

        if(CheckFields(arr)){
            ShowProgressDialog();

            Editable address = etAddress.getText();
            Editable topic = etTopic.getText();
            Editable port = etPort.getText();

            String PROTOCOL = "tcp://";
            mqttClient = new MyMqttClient(this, PROTOCOL + address + ":" + port);
            mqttClient.Subscribe(String.valueOf(topic));

            if(cbRemember.isChecked())
                SaveBroker();
        }
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
        SetUpSettingsActivity();
    }

    private void SetUpSettingsActivity() {
        currentView = R.layout.activity_settings;
        setContentView(currentView);

        etAddress = findViewById(R.id.etAddress);
        etPort = findViewById(R.id.etPort);
        etTopic = findViewById(R.id.etTopic);

        etAddress.setText(mqttClient.GetURL());
        etPort.setText(mqttClient.GetPort());
        etTopic.setText(mqttClient.GetTopic());

        TextView btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(this::BtnEditClick);
    }

    private void BtnEditClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.lbl_warning)
                .setMessage(R.string.lbl_warning_msg)
                .setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SetUpInitActivity();
                    }
                })
                .setNegativeButton(R.string.lbl_no, null)
                .setIcon(R.drawable.warning)
                .show();
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

    private void ShowProgressDialog(){
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.lbl_loading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void HideProgressDialog(){
        progressDialog.cancel();
    }

    private void HandleMessage(String topic, String msg){
        String[] all_data = msg.split("-");
        for(String data : all_data){
            String key = data.split(":")[0];
            try {
                double value = Double.parseDouble(data.split(":")[1]);
                switch (key) {
                    case TEMP_AMB_KEY:
                        temp_amb_list.add(value);
                        //Measurement m = new Measurement(value, new Date());
                        break;
                    case TEMP_OBJ_KEY:
                        temp_obj_list.add(value);
                        break;
                    case CO2_KEY:
                        co2_list.add(value);
                        break;
                    case SPO2_KEY:
                        spo2_list.add(value);
                        break;
                }
            } catch (Exception ignored){}
        }
    }

    @Override
    public void MessageArrived(String topic, String msg) {
        messages.add(topic + ": " + msg);
        //adapter.notifyDataSetChanged();
        //lvMsg.smoothScrollToPosition(messages.size()-1);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        HandleMessage(topic, msg);
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
        //SetUpMainActivity();
        SetUpHomeActivity();
        HideProgressDialog();
    }

    @Override
    public void ConnectionFailed() {
        HideProgressDialog();
        SetUpInitActivity();
        Toast.makeText(this, R.string.err_connect, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ConnectionLost() {
        Toast.makeText(this, R.string.lbl_conn_lost, Toast.LENGTH_LONG).show();
    }

    @Override
    public void SendData(int graph) {
        plotFragment = new PlotFragment();

        Bundle bundle = new Bundle();
        switch (graph){
            case TEMP_AMB:
                bundle.putInt(CASE_KEY, TEMP_AMB);
                bundle.putSerializable(DATA_KEY, temp_amb_list);
                break;
            case TEMP_OBJ:
                bundle.putInt(CASE_KEY, TEMP_OBJ);
                bundle.putSerializable(DATA_KEY, temp_obj_list);
                break;
            case CO2:
                bundle.putInt(CASE_KEY, CO2);
                bundle.putSerializable(DATA_KEY, co2_list);
                break;
            default:
                bundle.putInt(CASE_KEY, 0);
                bundle.putSerializable(DATA_KEY, new ArrayList<>());
        }

        plotFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
    }
}