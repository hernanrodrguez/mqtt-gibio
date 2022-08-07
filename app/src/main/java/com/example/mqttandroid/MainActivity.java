package com.example.mqttandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttListener, IComFragments{

    private MyMqttClient mqttClient;
    private ArrayList<Room> rooms;
    private ArrayList<Room> people;

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
    private CalibrateFragment calibrateFragment;
    private PersonFragment personFragment;

    private DrawerLayout drawerLayout;
    private Menu menu;
    private CheckBox cbRemember;

    private ProgressDialog progressDialog;

    private EditText dialogInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SetUpSplashScreen();
        messages = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        rooms = new ArrayList<>();
        people = new ArrayList<>();

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
                case R.layout.activity_main:
                    SetUpHomeActivity();
                    break;
                case R.layout.activity_send_message:
                    SetUpMainActivity();
                    break;
                default:
                    if (getSupportFragmentManager().getBackStackEntryCount() > 1)
                        getSupportFragmentManager().popBackStack();
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

        //Button btnAddNewBroker = findViewById(R.id.btnAddNewBroker);
        //btnAddNewBroker.setOnClickListener(this::btnAddNewBrokerClick);
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
        Toolbar toolbar = findViewById(R.id.toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.lbl_nav_open, R.string.lbl_nav_closed);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this::NavigationItemSelected);
        navigationView.setCheckedItem(R.id.nav_home);

        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, homeFragment).addToBackStack(null).commit();
    }

    private boolean NavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                SetUpHomeActivity();
                break;
            case R.id.nav_rooms:
                BtnClicked(Constants.ROOMS_ID);
                break;
            case R.id.nav_people:
                BtnClicked(Constants.PEOPLE_ID);
                break;
            case R.id.nav_map:
                BtnClicked(Constants.MAP_ID);
                break;
            case R.id.nav_settings:
                SetUpSettingsActivity();
                break;
            case R.id.nav_info:
                break;
            case R.id.nav_bug:
                SetUpMainActivity();
                break;
            case R.id.nav_calibration:
                BtnClicked(Constants.CALIBRATE_ID);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ShowRooms(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.room);
        builderSingle.setTitle(R.string.lbl_select_room);

        ArrayList<String> rooms_id = new ArrayList<>();
        for(Room room : rooms)
            rooms_id.add(room.GetIdRoom().toUpperCase());

        CharSequence[] cs = rooms_id.toArray(new CharSequence[rooms_id.size()]);

        builderSingle.setItems(cs, (dialog, which) -> {
            SendRoom(rooms.get(which));
        });

        builderSingle.setNegativeButton(R.string.lbl_cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builderSingle.create();
        dialog.show();
    }

    private void ShowPeople(int id){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.thermometer_person);
        builderSingle.setTitle(R.string.lbl_select_person);

        ArrayList<String> people_id = new ArrayList<>();
        for(Room person : people)
            people_id.add(person.GetIdRoom().toUpperCase());

        CharSequence[] cs = people_id.toArray(new CharSequence[people_id.size()]);

        switch (id){
            case Constants.PEOPLE_ID:
                builderSingle.setItems(cs, (dialog, which) -> {
                    SendPersonLastMeasurement(people.get(which), which);
                });
                break;
            case Constants.CORRELATION_ID:
                builderSingle.setItems(cs, (dialog, which) -> {
                    SetUpCorrelationPlot(which);
                });
                break;
            case Constants.TEMP_OBJ_ID:
            case Constants.TEMP_AMB_ID:
            case Constants.CO2_ID:
            case Constants.SPO2_ID:
            case Constants.HR_ID:
                builderSingle.setItems(cs, (dialog, which) -> {
                    SendRequest(id, which);
                });
                break;
        }

        builderSingle.setNegativeButton(R.string.lbl_cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builderSingle.create();
        dialog.show();
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

            String address = etAddress.getText().toString().trim();
            String topic = etTopic.getText().toString().trim();
            String port = etPort.getText().toString().trim();

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
            String topic = etTopicSend.getText().toString().trim();
            String payload = etMessage.getText().toString().trim();

            mqttClient.Publish(topic, payload);
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
            return topic.split("/")[2];
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
                    Measurement measurement;
                    if(id_meas == Constants.TEMP_AMB_ID || id_meas == Constants.CO2_ID){
                        Date date = Calendar.getInstance().getTime(); // Obtengo la hora de la medicion
                        measurement = new Measurement(value, date); // Creo la nueva medicion
                    } else{
                        int index = room.GetLastIndex(id_meas); // Obtengo el indice de la ultima medicion
                        measurement = new Measurement(value, index); // Creo la nueva medicion
                    }
                    room.Add(measurement, id_meas); // Guardo la nueva medicion
                    if(getSupportFragmentManager().findFragmentById(R.id.fragHome) instanceof PlotFragment)
                        plotFragment.MeasArrived(id_room, id_meas, measurement); // Envio la medicion al plot fragment para graficar en tiempo real
                }
            }
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        } // Ante un mensaje erroneo o algun problema, simplemente ignoro el caso
    }

    private boolean PersonExists(String id_person){
        for(Room person : people){
            if(person.GetIdRoom().equals(id_person))
                return true;
        }
        return false;
    }

    private int GetCurrentPerson(String id_person){
        for(int i=0; i<people.size(); i++){
            if(people.get(i).GetIdRoom().equals(id_person))
                return i;
        }
        return -1;
    }

    private void HandleMessage_Person(String topic, String msg){
        try {
            int current_person;
            String id_person = ObtainIdRoom(topic);
            if(id_person != null) {
                if (PersonExists(id_person)) {
                    current_person = GetCurrentPerson(id_person);
                } else {
                    current_person = people.size();
                    people.add(new Room(id_person));
                }
                String[] all_data = msg.split("-");

                for (String data : all_data) {
                    String key_meas = data.split(":")[0];
                    int id_meas = Constants.Key2Id(key_meas); // Obtengo el tipo de medicion
                    double value = Double.parseDouble(data.split(":")[1]); // Obtengo el valor de la medicion

                    Room person = people.get(current_person); // Trabajo con la persona a la que pertenece la medicion
                    Measurement measurement;
                    Date date = Calendar.getInstance().getTime(); // Obtengo la hora de la medicion
                    measurement = new Measurement(value, date); // Creo la nueva medicion
                    person.Add(measurement, id_meas); // Guardo la nueva medicion
                    if(getSupportFragmentManager().findFragmentById(R.id.fragHome) instanceof PlotFragment)
                        plotFragment.MeasArrived(id_person, id_meas, measurement); // Envio la medicion al plot fragment para graficar en tiempo real
                    if(getSupportFragmentManager().findFragmentById(R.id.fragHome) instanceof PersonFragment)
                        personFragment.MeasArrived(id_person, id_meas, measurement);
                }
            }
        } catch (Exception e){
            Log.e("ERROR", e.getMessage());
        } // Ante un mensaje erroneo o algun problema, simplemente ignoro el caso
    }

    private void HandleMessage_Calibration(String topic, String msg){
        try {
            int current_person;
            String id_person = ObtainIdRoom(topic);
            if(id_person != null) {
                current_person = GetCurrentPerson(id_person);
                int id_meas = Constants.Key2Id(msg.split(":")[0]);
                float value = Float.parseFloat(msg.split(":")[1]);
                ShowMeasurementDialog(value, id_meas, current_person);
            }
        } catch (Exception e){
            Toast.makeText(this, R.string.err_value, Toast.LENGTH_SHORT).show();
        } // Ante un mensaje erroneo o algun problema, simplemente ignoro el caso
    }

    private void SendPlotData(int id){
        if(rooms.size() > 0 && id == Constants.ROOMS_ID)
            CheckRooms();
        else if(people.size() > 0 && id == Constants.PEOPLE_ID)
            CheckPeople(id);
        else
            Toast.makeText(this, R.string.lbl_no_meas, Toast.LENGTH_SHORT).show();
    }

    private void SetUpCalibrateFragment(){
        calibrateFragment = new CalibrateFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, calibrateFragment).addToBackStack(null).commit();
    }

    private void DevicesArrived(String msg){
        try {
            JSONArray arr = new JSONArray(msg);
            for (int i = 0; i < arr.length(); i++) {
                int id = arr.getJSONObject(i).getInt("id");
                String key = arr.getJSONObject(i).getString("key");
                int tipo = arr.getJSONObject(i).getInt("tipo_dispositivo");
                Log.println(Log.DEBUG, "JSON", "DEVICE ARRIVED");
                Log.println(Log.DEBUG, "JSON", "id: " + id);
                Log.println(Log.DEBUG, "JSON", "key: " + key);
                Log.println(Log.DEBUG, "JSON", "tipo_dispositivo: " + tipo);
            }
        } catch (Exception e){
            Log.e("ERROR", e.getLocalizedMessage());
        }
    }

    @Override
    public void MessageArrived(String topic, String msg) {
        if(topic.split("/")[1].equals("dispositivos"))
            DevicesArrived(msg);
        //else if(topic.split("/")[1].equals("dispo"))
        //    HandleMessage_Person(topic, msg);
//        try{
//            if(topic.split("/")[1].equals("room"))
//                HandleMessage(topic, msg);
//            else if(topic.split("/")[1].equals("person"))
//                HandleMessage_Person(topic, msg);
//            if(topic.split("/")[3].equals("meas") &&
//                getSupportFragmentManager().findFragmentById(R.id.fragHome) instanceof CalibrateFragment) {
//                HideProgressDialog();
//                HandleMessage_Calibration(topic, msg);
//            }
//        } catch (Exception e){
//            Log.e("ERROR", e.getLocalizedMessage());
//        }
//        messages.add(topic + ": " + msg);
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void MessageSent() {

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

    private void ShowMeasurementDialog(float value, int id_meas, int room){
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null);

        TextView dialogTitle = (TextView) viewInflated.findViewById(R.id.dialog_title);
        TextView dialogDesc = (TextView) viewInflated.findViewById(R.id.dialog_desc);
        TextView dialogBtn = (TextView) viewInflated.findViewById(R.id.dialog_btn);
        dialogInput = (EditText) viewInflated.findViewById(R.id.dialog_input);

        switch (id_meas){
            case Constants.TEMP_OBJ_ID:
                dialogTitle.setText(R.string.lbl_tobj_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, "°C"));
                break;
            case Constants.TEMP_AMB_ID:
                dialogTitle.setText(R.string.lbl_tamb_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, "°C"));
                break;
            case Constants.CO2_ID:
                dialogTitle.setText(R.string.lbl_co2_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, " ppm"));
                break;
            case Constants.SPO2_ID:
                dialogTitle.setText(R.string.lbl_spo2_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, "%"));
                break;
            case Constants.HR_ID:
                dialogTitle.setText(R.string.lbl_hr_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, " bpm"));
                break;
        }

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<EditText> arr = new ArrayList<EditText>(){{ add(dialogInput); }};
                if(CheckFields(arr)){
                    float real_value = Float.parseFloat(dialogInput.getText().toString().trim());
                    SaveMeasurement(real_value, value, id_meas, room);
                    dialog.dismiss();
                }
            }
        });
        dialog.setView(viewInflated);
        dialog.show();
    }

    private String ComposeString(float val, int id_meas, int room){
        try {
            return people.get(room).GetIdRoom() +
                    ":" +
                    Constants.Id2Key(id_meas) +
                    ":" +
                    String.format(java.util.Locale.US,"%.2f", val);
        } catch (Exception e){
            Toast.makeText(this, R.string.err_send, Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    private void SaveMeasurement(float real_value, float meas_value, int id_meas, int room){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        String real_values = prefs.getString(Constants.REAL_VALUES_KEY, "");
        String meas_values = prefs.getString(Constants.MEAS_VALUES_KEY, "");

        if(real_values.length() == 0) {
            real_values = ComposeString(real_value, id_meas, room);
            meas_values = ComposeString(meas_value, id_meas, room);
        } else {
            real_values += ("-" + ComposeString(real_value, id_meas, room));
            meas_values += ("-" + ComposeString(meas_value, id_meas, room));
        }

        editor.putString(Constants.REAL_VALUES_KEY, real_values);
        editor.putString(Constants.MEAS_VALUES_KEY, meas_values);
        editor.apply();
    }

    private String CleanValues(String values, String id_person){
        StringBuilder ret = new StringBuilder();
        for(String meas : values.split("-")){
            String[] data = meas.split(":");
            if(id_person.equals(data[0])){
                ret.append(data[1]).append(":").append(data[2]).append("-");
            }
        }
        return ret.toString();
    }

    private void SetUpCorrelationPlot(int id_person){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String str_id_person = people.get(id_person).GetIdRoom();
        String real_values = prefs.getString(Constants.REAL_VALUES_KEY, "");
        String meas_values = prefs.getString(Constants.MEAS_VALUES_KEY, "");

        real_values = CleanValues(real_values, str_id_person);
        meas_values = CleanValues(meas_values, str_id_person);

        if(!real_values.equals("") && !meas_values.equals("")){
            plotFragment = new PlotFragment();
            Bundle bundle = new Bundle();

            bundle.putInt(Constants.CASE_KEY, Constants.CORRELATION_ID);
            bundle.putString(Constants.REAL_VALUES_KEY, real_values);
            bundle.putString(Constants.MEAS_VALUES_KEY, meas_values);

            plotFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
        } else {
            Toast.makeText(this, R.string.lbl_no_corr, Toast.LENGTH_SHORT).show();
        }
    }

    private void MqttRequestPeople() {
        try{
            mqttClient.Publish("request/dispositivos",
                                "2");
            //ShowProgressDialog();
        } catch (Exception e){
            Log.println(Log.ERROR, "ERROR", "Request Measurement Exception");
        }
    }

    private void MqttRequestRooms() {
        try{
            mqttClient.Publish("request/dispositivos",
                    "1");
            //ShowProgressDialog();
        } catch (Exception e){
            Log.println(Log.ERROR, "ERROR", "Request Measurement Exception");
        }
    }

    @Override
    public void BtnClicked(int id) {
        switch (id){
            case Constants.ROOMS_ID:
                MqttRequestRooms();
                //SendPlotData(id);
                break;
            case Constants.PEOPLE_ID:
            case Constants.CORRELATION_ID:
                MqttRequestPeople();
                //CheckPeople(id);
                break;
            case Constants.MAP_ID:
                break;
            case Constants.SETTINGS_ID:
                SetUpSettingsActivity();
                break;
            case Constants.CALIBRATE_ID:
                SetUpCalibrateFragment();
                break;
            default:
                break;
        }
    }

    @Override
    public void BtnClicked(int graph, int id) {
        switch (graph) {
            case Constants.PERSON_ID:
                SendPerson(people.get(id));
                break;
            case Constants.TEMP_OBJ_ID:
                SendMeasList(graph, people.get(id).GetTObjList());
                break;
            case Constants.SPO2_ID:
                SendMeasList(graph, people.get(id).GetSpo2List());
                break;
            case Constants.TEMP_AMB_ID:
                SendMeasList(graph, people.get(id).GetTAmbList());
                break;
            case Constants.CO2_ID:
                SendMeasList(graph, people.get(id).GetCo2List());
                break;
            case Constants.HR_ID:
                SendMeasList(graph, people.get(id).GetHRList());
                break;
            default:
                break;
        }
    }

    @Override
    public void SendRoom(Room room) {
        plotFragment = new PlotFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, Constants.ROOMS_ID);
        bundle.putSerializable(Constants.DATA_KEY, room);

        plotFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
    }

    @Override
    public void SendPerson(Room person) {
        plotFragment = new PlotFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, Constants.PEOPLE_ID);
        bundle.putSerializable(Constants.DATA_KEY, person);

        plotFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
    }

    @Override
    public void SendMeasList(int graph, MeasList list) {
        plotFragment = new PlotFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, graph);
        bundle.putSerializable(Constants.DATA_KEY, list);

        plotFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
    }

    @Override
    public void RequestMeasurement(int id) {
        CheckPeople(id);
    }

    @Override
    public void ClearCalibrationData(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.lbl_warning)
                .setMessage(R.string.lbl_warning_cal)
                .setPositiveButton(R.string.lbl_yes, (dialog, which) -> ClearData())
                .setNegativeButton(R.string.lbl_no, null)
                .setIcon(R.drawable.warning)
                .show();
    }

    public void ClearData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.MEAS_VALUES_KEY, "");
        editor.putString(Constants.REAL_VALUES_KEY, "");
        editor.apply();
    }

    private ArrayList<MeasList> GetListsById(int id){
        ArrayList<MeasList> list = new ArrayList<>();
        for(Room room : rooms)
            list.add(room.GetList(id));
        return list;
    }

    private ArrayList<MeasList> GetPersonLists(){
        ArrayList<MeasList> list = new ArrayList<>();
        for(Room room : rooms) {
            list.add(room.GetTObjList());
            list.add(room.GetSpo2List());
        }
        return list;
    }

    private void CheckRooms(){
        if(rooms.size() > 1)
            ShowRooms();
        else
            SendRoom(rooms.get(0));
    }

    private void CheckPeople(int id){
        if(people.size() > 1)
            ShowPeople(id);
        else if(people.size() == 1){
            switch (id){
                case Constants.PEOPLE_ID:
                    SendPersonLastMeasurement(people.get(0), 0);
                    break;
                case Constants.CORRELATION_ID:
                    SetUpCorrelationPlot(0);
                    break;
                case Constants.TEMP_OBJ_ID:
                case Constants.TEMP_AMB_ID:
                case Constants.CO2_ID:
                case Constants.SPO2_ID:
                case Constants.HR_ID:
                    SendRequest(id, 0);
                    break;
            }
        } else
            Toast.makeText(this, R.string.lbl_no_meas, Toast.LENGTH_SHORT).show();
    }

    private void SendRequest(int id_meas, int id_person){
        try{
            mqttClient.Publish(
                    mqttClient.GetBaseTopic() +
                            "/person/" +
                            people.get(id_person).GetIdRoom() +
                            "/cal",
                    Constants.Id2Key(id_meas));
            ShowProgressDialog();
        } catch (Exception e){
            Log.println(Log.ERROR, "ERROR", "Request Measurement Exception");
        }

    }

    private void SendPersonLastMeasurement(Room person, int id_person) {
        personFragment = new PersonFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, id_person);
        bundle.putSerializable(Constants.DATA_KEY, person);

        personFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, personFragment).addToBackStack(null).commit();
    }
}