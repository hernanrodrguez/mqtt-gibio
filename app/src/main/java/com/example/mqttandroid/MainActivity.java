package com.example.mqttandroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MqttListener, IComFragments{

    private MyMqttClient mqttClient;
    private ArrayList<Dispositivo> dispositivos;
    private ArrayList<Dispositivo> habitaciones;
    private ArrayList<Dispositivo> personas;

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

    private int lastBtnClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setUpSplashScreen();
        messages = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        habitaciones = new ArrayList<>();
        personas = new ArrayList<>();
        dispositivos = new ArrayList<>();

        if(!getBroker())
            setUpInitActivity();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            switch (currentView){
                case R.layout.activity_settings:
                case R.layout.activity_main:
                    setUpHomeActivity();
                    break;
                case R.layout.activity_send_message:
                    setUpMainActivity();
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

    private boolean getBroker(){
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

    private void saveBroker(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.BROKER_KEY, mqttClient.GetServerURL());
        editor.putString(Constants.TOPIC_KEY, mqttClient.GetTopic());
        editor.apply();
    }

    private void setUpSplashScreen(){
        currentView = R.layout.splash_screen;
        setContentView(currentView);
    }

    private void setUpMainActivity(){
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

    private void setUpHomeActivity(){
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
        navigationView.setNavigationItemSelectedListener(this::navigationItemSelected);
        navigationView.setCheckedItem(R.id.nav_home);

        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, homeFragment).addToBackStack(null).commit();
    }

    private boolean navigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                setUpHomeActivity();
                break;
            case R.id.nav_rooms:
                btnClicked(Constants.DISPO_HABITACION);
                break;
            case R.id.nav_people:
                btnClicked(Constants.DISPO_PERSONA);
                break;
            case R.id.nav_map:
                btnClicked(Constants.MAP_ID);
                break;
            case R.id.nav_settings:
                setUpSettingsActivity();
                break;
            case R.id.nav_info:
                break;
            case R.id.nav_bug:
                setUpMainActivity();
                break;
            case R.id.nav_calibration:
                btnClicked(Constants.CALIBRATE_ID);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void mostrarDispositivos(int tipo_dispositivo){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.room);
        if(tipo_dispositivo == Constants.DISPO_HABITACION)
            builderSingle.setTitle(R.string.lbl_select_room);
        if(tipo_dispositivo == Constants.DISPO_PERSONA)
            builderSingle.setTitle(R.string.lbl_select_person);

        ArrayList<String> dispositivos_key = new ArrayList<>();
        for(Dispositivo d : dispositivos) {
            if(tipo_dispositivo == Constants.DISPO_HABITACION && d.getTipoDispositivo() == Constants.DISPO_HABITACION) {
                dispositivos_key.add(d.getKey().toUpperCase());
            }
            if(tipo_dispositivo == Constants.DISPO_PERSONA && d.getTipoDispositivo() == Constants.DISPO_PERSONA) {
                dispositivos_key.add(d.getKey().toUpperCase());
            }
        }
        CharSequence[] cs = dispositivos_key.toArray(new CharSequence[dispositivos_key.size()]);

        builderSingle.setItems(cs, (dialog, which) -> {
            Log.println(Log.DEBUG, "MOSTRAR DISPOSITIVOS", "Click en " + dispositivos_key.get(which));
            for(Dispositivo d : dispositivos){
                Log.println(Log.DEBUG, "MOSTRAR DISPOSITIVOS", "Iterando " + d.toString());
                if(d.getKey().toUpperCase().equals(dispositivos_key.get(which))){
                    Log.println(Log.DEBUG, "MOSTRAR DISPOSITIVOS", "Envio " + d.toString());
                    if(tipo_dispositivo == Constants.DISPO_HABITACION)
                        mqttRequestMediciones(d, Constants.GRAFICAR_HABITACION);
                    if(tipo_dispositivo == Constants.DISPO_PERSONA)
                        mqttRequestMediciones(d, Constants.ULTIMAS_MEDICIONES);
                    break;
                }
            }
        });

        builderSingle.setNegativeButton(R.string.lbl_cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builderSingle.create();
        dialog.show();
    }

    private void setUpInitActivity(){
        currentView = R.layout.activity_init;
        setContentView(currentView);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        etAddress = findViewById(R.id.etAddressInit);
        etPort = findViewById(R.id.etPortInit);
        etTopic = findViewById(R.id.etTopicInit);
        cbRemember = findViewById(R.id.cbRemember);
        TextView btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(this::btnEnterClick);
    }

    private void setUpSendMessageActivity() {
        currentView = R.layout.activity_send_message;
        setContentView(currentView);

        etMessage = findViewById(R.id.etMsg);
        etMessage.setText(R.string.lbl_test_message);
        etTopicSend = findViewById(R.id.etTopicSend);
        etTopicSend.setText(R.string.lbl_test_topic_send);

        Button btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this::btnSendClick);
    }

    private void setUpSettingsActivity() {
        currentView = R.layout.activity_settings;
        setContentView(currentView);

        etAddress = findViewById(R.id.etAddress);
        etPort = findViewById(R.id.etPort);
        etTopic = findViewById(R.id.etTopic);

        etAddress.setText(mqttClient.GetURL());
        etPort.setText(mqttClient.GetPort());
        etTopic.setText(mqttClient.GetTopic());

        TextView btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(this::btnEditClick);
    }

    private void btnEnterClick(View view) {
        ArrayList<EditText> arr = new ArrayList<EditText>(){
            {
                add(etAddress);
                add(etPort);
                add(etTopic);
            }
        };

        if(checkFields(arr)){
            showProgressDialog();

            String address = etAddress.getText().toString().trim();
            String topic = etTopic.getText().toString().trim();
            String port = etPort.getText().toString().trim();

            String PROTOCOL = "tcp://";
            mqttClient = new MyMqttClient(this, PROTOCOL + address + ":" + port);
            mqttClient.Subscribe(String.valueOf(topic));

            if(cbRemember.isChecked())
                saveBroker();
        }
    }

    private void btnSendMessageClick(View view) {
        if(mqttClient == null){
            Toast.makeText(this, R.string.err_broker, Toast.LENGTH_SHORT).show();
        } else {
            setUpSendMessageActivity();
        }
    }

    private void btnSendClick(View view) {
        ArrayList<EditText> arr = new ArrayList<EditText>(){
            {
                add(etTopicSend);
                add(etMessage);
            }
        };

        if(checkFields(arr)){
            String topic = etTopicSend.getText().toString().trim();
            String payload = etMessage.getText().toString().trim();

            mqttClient.Publish(topic, payload);
            setUpMainActivity();
        }
    }

    private void btnAddNewBrokerClick(View view) {
        setUpSettingsActivity();
    }

    private void btnEditClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.lbl_warning)
                .setMessage(R.string.lbl_warning_msg)
                .setPositiveButton(R.string.lbl_yes, (dialog, which) -> setUpInitActivity())
                .setNegativeButton(R.string.lbl_no, null)
                .setIcon(R.drawable.warning)
                .show();
    }

    private boolean checkFields(List<EditText> editTexts){
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

    private void showProgressDialog(){
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.lbl_loading));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void hideProgressDialog(){
        progressDialog.cancel();
    }

    private String ObtainIdRoom(String topic){
        try{
            return topic.split("/")[2];
        } catch (Exception e){
            return null;
        }
    }

    private void mostrarPersonas(int id){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.drawable.thermometer_person);
        builderSingle.setTitle(R.string.lbl_select_person);

        ArrayList<String> people_keys = new ArrayList<>();
        for(Dispositivo persona : personas)
            people_keys.add(persona.getKey().toUpperCase());

        CharSequence[] cs = people_keys.toArray(new CharSequence[people_keys.size()]);

        switch (id){
            case Constants.DISPO_PERSONA:
                builderSingle.setItems(cs, (dialog, which) -> {
                    SendPersonLastMeasurement(personas.get(which), which);
                });
                break;
            case Constants.CORRELATION_ID:
                builderSingle.setItems(cs, (dialog, which) -> {
                    SetUpCorrelationPlot(which);
                });
                break;
            /*case Constants.TEMPERATURA_SUJETO:
            case Constants.TEMPERATURA_AMBIENTE:
            case Constants.CO2:
            case Constants.SPO2:
            case Constants.FRECUENCIA_CARDIACA:
                builderSingle.setItems(cs, (dialog, which) -> {
                    SendRequest(id, which);
                });
                break;*/
        }

        builderSingle.setNegativeButton(R.string.lbl_cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builderSingle.create();
        dialog.show();
    }

    private boolean habitacionExiste(String key_habitacion){
        for(Dispositivo dispositivo : habitaciones){
            if(dispositivo.getKey().equals(key_habitacion))
                return true;
        }
        return false;
    }

    private int getHabitacionActual(String key_habitacion){
        for(int i = 0; i< habitaciones.size(); i++){
            if(habitaciones.get(i).getKey().equals(key_habitacion))
                return i;
        }
        return -1;
    }

    private boolean personaExiste(String key_persona){
        for(Dispositivo persona : personas){
            if(persona.getKey().equals(key_persona))
                return true;
        }
        return false;
    }

    private int getPersonaActual(String key_persona){
        for(int i = 0; i< personas.size(); i++){
            if(personas.get(i).getKey().equals(key_persona))
                return i;
        }
        return -1;
    }

    /*
    private void HandleMessage_Calibration(String topic, String msg){
        try {
            int current_person;
            String id_person = ObtainIdRoom(topic);
            if(id_person != null) {
                current_person = getPersonaActual(id_person);
                int id_meas = Constants.Key2Id(msg.split(":")[0]);
                float value = Float.parseFloat(msg.split(":")[1]);
                ShowMeasurementDialog(value, id_meas, current_person);
            }
        } catch (Exception e){
            Toast.makeText(this, R.string.err_value, Toast.LENGTH_SHORT).show();
        } // Ante un mensaje erroneo o algun problema, simplemente ignoro el caso
    }
    */
    private void verificarDispositivos(){
        if(dispositivos.size() > 0) {
            int personas = 0;
            int habitaciones = 0;
            for (Dispositivo d : dispositivos) {
                if (d.getTipoDispositivo() == Constants.DISPO_PERSONA) personas++;
                if (d.getTipoDispositivo() == Constants.DISPO_HABITACION) habitaciones++;
            }
            Log.println(Log.DEBUG, "DEBUG", "personas: " + personas);
            Log.println(Log.DEBUG, "DEBUG", "habitaciones: " + habitaciones);
            if (lastBtnClicked == Constants.DISPO_HABITACION) {
                if (habitaciones > 0) mostrarDispositivos(lastBtnClicked);
                else Toast.makeText(this, R.string.lbl_no_meas, Toast.LENGTH_SHORT).show();
            } else if (lastBtnClicked == Constants.DISPO_PERSONA) {
                if (personas > 0) mostrarDispositivos(lastBtnClicked);
                else Toast.makeText(this, R.string.lbl_no_meas, Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(this, R.string.lbl_no_meas, Toast.LENGTH_SHORT).show();
    }

    private void SetUpCalibrateFragment(){
        calibrateFragment = new CalibrateFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, calibrateFragment).addToBackStack(null).commit();
    }

    public static boolean containsId(ArrayList<Dispositivo> dispositivos, int id) {
        for(Dispositivo d : dispositivos) {
            if(d != null && d.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsMedicion(Dispositivo d, Medicion m, int tipo_medicion) {
        for(Medicion medicion : d.getArray(tipo_medicion).getMediciones()) {
            if(medicion.getDate().equals(m.getDate())) {
                return true;
            }
        }
        return false;
    }

    private void guardarDispositivos(String msg){
        try {
            JSONArray arr = new JSONArray(msg);
            for (int i = 0; i < arr.length(); i++) {
                int id = arr.getJSONObject(i).getInt("id");
                String key = arr.getJSONObject(i).getString("key");
                int tipo = arr.getJSONObject(i).getInt("tipo_dispositivo");
                Dispositivo d = new Dispositivo(id, key, tipo);
                Log.println(Log.DEBUG, "LLEGO DISPOSITIVO", d.toString());
                if (!containsId(dispositivos, id))
                    dispositivos.add(d);
            }
            verificarDispositivos();
        } catch (Exception e){
            Log.e("ERROR", e.getLocalizedMessage());
        }
    }

    private Dispositivo guardarMediciones(String msg){
        Dispositivo d = new Dispositivo();
        try {
            int id_dispositivo = 0;
            JSONArray arr = new JSONArray(msg);
            for (int i = 0; i < arr.length(); i++) {
                double valor = arr.getJSONObject(i).getDouble("valor");
                int tipo_medicion = arr.getJSONObject(i).getInt("tipo_medicion");
                id_dispositivo = arr.getJSONObject(i).getInt("id_dispositivo");
                long timestamp = arr.getJSONObject(i).getLong("fecha");
                Date fecha = new Date(timestamp*1000);
                Medicion m = new Medicion(valor, fecha);
                d = getDispositivo(id_dispositivo);
                if(d != null) {
                    if(!containsMedicion(d, m, tipo_medicion)) {
                        d.addMedicion(m, tipo_medicion);
                        Log.println(Log.DEBUG, "GUARDO MEDICION", m.toString());
                        Log.println(Log.DEBUG, "GUARDO MEDICION", d.toString());
                    }
                }
            }
            return d;
        } catch (Exception e){
            Log.e("ERROR", e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public void MessageArrived(String topic, String msg) {
        hideProgressDialog();
        if(topic.split("/")[1].equals("dispositivos"))
            guardarDispositivos(msg);
        else if(topic.split("/")[1].equals("mediciones")) {
            Dispositivo d = guardarMediciones(msg);
            try {
                if (topic.split("/")[2].equals("last")) {
                    Log.println(Log.DEBUG, "LLEGO MENSAJE", "Ultima medicion");
                    sendDispositivo(d);
                }
            } catch (Exception e) {
                Log.d("LLEGO MENSAJE", "Mediciones de " + d.toString());
                if(lastBtnClicked == Constants.GRAFICAR_PERSONA)
                    sendPersona(d);
                else if(lastBtnClicked == Constants.DISPO_HABITACION)
                    sendDispositivo(d);
                else
                    sendArray(d, lastBtnClicked);
            }
        }
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
        setUpHomeActivity();
        hideProgressDialog();
    }

    @Override
    public void ConnectionFailed() {
        hideProgressDialog();
        setUpInitActivity();
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
            case Constants.TEMPERATURA_SUJETO:
                dialogTitle.setText(R.string.lbl_tobj_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, "°C"));
                break;
            case Constants.TEMPERATURA_AMBIENTE:
                dialogTitle.setText(R.string.lbl_tamb_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, "°C"));
                break;
            case Constants.CO2:
                dialogTitle.setText(R.string.lbl_co2_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, " ppm"));
                break;
            case Constants.SPO2:
                dialogTitle.setText(R.string.lbl_spo2_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, "%"));
                break;
            case Constants.FRECUENCIA_CARDIACA:
                dialogTitle.setText(R.string.lbl_hr_received);
                dialogDesc.setText(getString(R.string.lbl_value_received, value, " bpm"));
                break;
        }

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<EditText> arr = new ArrayList<EditText>(){{ add(dialogInput); }};
                if(checkFields(arr)){
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
            return personas.get(room).getKey() +
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

        String str_id_person = personas.get(id_person).getKey();
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

    private void mqttRequestMediciones(Dispositivo d, int tipo_request) {
        Date date = new Date();
        Date today = new Date(date.getYear(), date.getMonth(), date.getDate(), 0, 0, 0);
        try{
            switch(tipo_request){
                case Constants.TEMPERATURA_SUJETO:
                case Constants.TEMPERATURA_AMBIENTE:
                case Constants.CO2:
                case Constants.SPO2:
                case Constants.FRECUENCIA_CARDIACA:
                    mqttClient.Publish("request/mediciones",
                            "id_dispositivo="+d.getId()+" AND "+
                                    "tipo_medicion="+tipo_request+" AND "+
                                    "fecha>"+today.getTime()/1000);
                    showProgressDialog();
                    break;
                case Constants.GRAFICAR_PERSONA:
                case Constants.GRAFICAR_HABITACION:
                    mqttClient.Publish("request/mediciones",
                            "id_dispositivo="+d.getId()+" AND "+
                                    "fecha>"+today.getTime()/1000);
                    showProgressDialog();
                    break;
                case Constants.ULTIMAS_MEDICIONES:
                    mqttClient.Publish("request/mediciones/last",
                            "id_dispositivo="+d.getId());
                    showProgressDialog();
                    break;
            }
        } catch (Exception e){
            Log.e("mqttRequestMediciones", e.getMessage());
        }
    }

    private void mqttRequestPersonas() {
        try{
            mqttClient.Publish("request/dispositivos",
                                "2");
            //showProgressDialog();
        } catch (Exception e){
            Log.println(Log.ERROR, "ERROR", "Request Personas Exception");
        }
    }

    private void mqttRequestHabitaciones() {
        try{
            mqttClient.Publish("request/dispositivos",
                    "1");
            showProgressDialog();
        } catch (Exception e){
            Log.println(Log.ERROR, "ERROR", "Request Habitaciones Exception");
        }
    }

    private Dispositivo getDispositivo(int id){
        for(Dispositivo d : dispositivos){
            if(d.getId() == id)
                return d;
        }
        return null;
    }

    @Override
    public void btnClicked(int id) {
        lastBtnClicked = id;
        switch (id){
            case Constants.DISPO_HABITACION:
                mqttRequestHabitaciones();
                break;
            case Constants.DISPO_PERSONA:
            case Constants.CORRELATION_ID:
                mqttRequestPersonas();
                //CheckPeople(id);
                break;
            case Constants.MAP_ID:
                break;
            case Constants.SETTINGS_ID:
                setUpSettingsActivity();
                break;
            case Constants.CALIBRATE_ID:
                SetUpCalibrateFragment();
                break;
            default:
                break;
        }
    }

    @Override
    public void btnClicked(int graph, int id) {
        lastBtnClicked = graph;
        Dispositivo d = getDispositivo(id);
        switch (graph) {
            case Constants.GRAFICAR_PERSONA:
            case Constants.TEMPERATURA_SUJETO:
            case Constants.TEMPERATURA_AMBIENTE:
            case Constants.CO2:
            case Constants.SPO2:
            case Constants.FRECUENCIA_CARDIACA:
                mqttRequestMediciones(d, graph);
                break;
            default:
                break;
        }
    }

    @Override
    public void sendDispositivo(Dispositivo dispositivo) {
        Bundle bundle = new Bundle();
        switch(lastBtnClicked){
            case Constants.DISPO_HABITACION:
                plotFragment = new PlotFragment();

                Log.println(Log.DEBUG, "SEND DISPOSITIVO", dispositivo.toString());
                bundle.putInt(Constants.CASE_KEY, Constants.GRAFICAR_HABITACION);
                bundle.putSerializable(Constants.DATA_KEY, dispositivo);

                plotFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
                break;
            case Constants.DISPO_PERSONA:
                personFragment = new PersonFragment();

                bundle.putInt(Constants.CASE_KEY, dispositivo.getId());
                bundle.putSerializable(Constants.DATA_KEY, dispositivo);

                personFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, personFragment).addToBackStack(null).commit();
                break;
        }

    }

    @Override
    public void sendArray(Dispositivo dispositivo, int graph) {
        Bundle bundle = new Bundle();
        plotFragment = new PlotFragment();

        Log.println(Log.DEBUG, "SEND LIST", dispositivo.toString());

        if (dispositivo.getArray(graph).size() > 0){
            bundle.putInt(Constants.CASE_KEY, graph);
            bundle.putSerializable(Constants.DATA_KEY, dispositivo.getArray(graph));

            plotFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
        } else {
            Toast.makeText(this, "No hay mediciones hoy", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void sendPersona(Dispositivo persona) {
        plotFragment = new PlotFragment();
        Bundle bundle = new Bundle();

        if(persona.tieneMediciones()){
            bundle.putInt(Constants.CASE_KEY, Constants.GRAFICAR_PERSONA);
            bundle.putSerializable(Constants.DATA_KEY, persona);

            plotFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
        } else {
            Toast.makeText(this, "No hay mediciones hoy", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void SendMeasList(int graph, ArrayMediciones list) {
        plotFragment = new PlotFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, graph);
        bundle.putSerializable(Constants.DATA_KEY, list);

        plotFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, plotFragment).addToBackStack(null).commit();
    }

    @Override
    public void RequestMeasurement(int id) {
        // CheckPeople(id);
    }

    @Override
    public void clearCalibrationData(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.lbl_warning)
                .setMessage(R.string.lbl_warning_cal)
                .setPositiveButton(R.string.lbl_yes, (dialog, which) -> clearData())
                .setNegativeButton(R.string.lbl_no, null)
                .setIcon(R.drawable.warning)
                .show();
    }

    public void clearData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.MEAS_VALUES_KEY, "");
        editor.putString(Constants.REAL_VALUES_KEY, "");
        editor.apply();
    }

    private ArrayList<ArrayMediciones> GetListsById(int id){
        ArrayList<ArrayMediciones> list = new ArrayList<>();
        for(Dispositivo dispositivo : habitaciones)
            list.add(dispositivo.getArray(id));
        return list;
    }

    private ArrayList<ArrayMediciones> GetPersonLists(){
        ArrayList<ArrayMediciones> list = new ArrayList<>();
        for(Dispositivo dispositivo : habitaciones) {
            list.add(dispositivo.getTObjArray());
            list.add(dispositivo.getSpo2Array());
        }
        return list;
    }

    private void CheckPeople(int id){
        if(personas.size() > 1)
            mostrarPersonas(id);
        else if(personas.size() == 1){
            switch (id){
                case Constants.DISPO_PERSONA:
                    SendPersonLastMeasurement(personas.get(0), 0);
                    break;
                case Constants.CORRELATION_ID:
                    SetUpCorrelationPlot(0);
                    break;
                /*case Constants.TEMPERATURA_SUJETO:
                case Constants.TEMPERATURA_AMBIENTE:
                case Constants.CO2:
                case Constants.SPO2:
                case Constants.FRECUENCIA_CARDIACA:
                    SendRequest(id, 0);
                    break;*/
            }
        } else
            Toast.makeText(this, R.string.lbl_no_meas, Toast.LENGTH_SHORT).show();
    }

    private void SendRequest(int id_meas, int id_person){
        try{
            mqttClient.Publish(
                    mqttClient.GetBaseTopic() +
                            "/person/" +
                            personas.get(id_person).getKey() +
                            "/cal",
                    Constants.Id2Key(id_meas));
            showProgressDialog();
        } catch (Exception e){
            Log.println(Log.ERROR, "ERROR", "Request Measurement Exception");
        }

    }

    private void SendPersonLastMeasurement(Dispositivo person, int id_person) {
        personFragment = new PersonFragment();
        Bundle bundle = new Bundle();

        bundle.putInt(Constants.CASE_KEY, id_person);
        bundle.putSerializable(Constants.DATA_KEY, person);

        personFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragHome, personFragment).addToBackStack(null).commit();
    }
}