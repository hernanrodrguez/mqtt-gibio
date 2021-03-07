package com.example.mqttandroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttListener, IComFragments{

    private MyMqttClient mqttClient;
    private ArrayList<Room> rooms;

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
    private Menu menu;
    private CheckBox cbRemember;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SetUpSplashScreen();
        messages = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        rooms = new ArrayList<>();

        if(!GetBroker())
            SetUpInitActivity();
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

        String URL = prefs.getString(Constants.BROKER_KEY, "");
        String topic = prefs.getString(Constants.TOPIC_KEY, "");
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
        editor.putString(Constants.BROKER_KEY, mqttClient.GetServerURL());
        editor.putString(Constants.TOPIC_KEY, mqttClient.GetTopic());
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
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView textView = findViewById(R.id.textView);
        Toolbar toolbar = findViewById(R.id.toolbar);

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
                SendData(Constants.TEMP_AMB_ID);
                break;
            case R.id.nav_person:
                SendData(Constants.TEMP_OBJ_ID);
                break;
            case R.id.nav_co2:
                SendData(Constants.CO2_ID);
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
        TextView btnEnter = findViewById(R.id.btnEnter);
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
                .setPositiveButton(R.string.lbl_yes, (dialog, which) -> SetUpInitActivity())
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

    private String ObtainIdRoom(String topic){
        try{
            return topic.split("/")[1];
        } catch (Exception e){
            return null;
        }
    }

    private boolean RoomExists(String id_room){
        for(Room room : rooms){
            if(room.GetIdRoom().equals(id_room))
                return true;
        }
        return false;
    }

    private int GetCurrentRoom(String id_room){
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).GetIdRoom().equals(id_room))
                return i;
        }
        return -1;
    }

    private void HandleMessage(String topic, String msg){
        try {
            int current_room;
            String id_room = ObtainIdRoom(topic);
            if(id_room != null) {
                if (RoomExists(id_room)) {
                    current_room = GetCurrentRoom(id_room);
                } else {
                    current_room = rooms.size();
                    rooms.add(new Room(id_room));
                }
                String[] all_data = msg.split("-");

                for (String data : all_data) {
                    String key_meas = data.split(":")[0];
                    int id_meas = Constants.Key2Id(key_meas); // Obtengo el tipo de medicion
                    double value = Double.parseDouble(data.split(":")[1]); // Obtengo el valor de la medicion

                    Room room = rooms.get(current_room); // Trabajo con la habitacion a la que pertenece la medicion
                    int index = room.GetLastIndex(id_meas); // Obtengo el indice de la ultima medicion

                    Measurement measurement = new Measurement(value, index); // Creo la nueva medicion
                    room.Add(measurement, id_meas); // Guardo la nueva medicion

                    plotFragment.MeasArrived(id_room, id_meas, measurement); // Envio la medicion al plot fragment para graficar en tiempo real
                }
            }
        } catch (Exception ignored){} // Ante un mensaje erroneo o algun problema, simplemente ignoro el caso
    }

    @Override
    public void MessageArrived(String topic, String msg) {
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
    public void SendData(int id) {
        plotFragment = new PlotFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, id);
        bundle.putInt(Constants.QUANT_KEY, rooms.size());

        for(int i=0; i<rooms.size(); i++)
            bundle.putSerializable(Constants.DATA_KEY + i, rooms.get(i).GetList(id));

        plotFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
    }
}