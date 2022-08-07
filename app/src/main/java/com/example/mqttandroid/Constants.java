package com.example.mqttandroid;

import java.util.ArrayList;

public final class Constants {

    public final static String BROKER_KEY = "Broker";
    public final static String TOPIC_KEY = "Topic";
    public final static String DATA_KEY = "Data";
    public final static String CASE_KEY = "Case";
    public final static String QUANT_KEY = "Quant";
    public final static String REAL_VALUES_KEY = "Real";
    public final static String MEAS_VALUES_KEY = "Meas";
    public final static String FACTOR_KEY = "Factor";

    public final static String KEY_TEMPERATURA_SUJETO = "TO";
    public final static String KEY_TEMPERATURA_AMBIENTE = "TA";
    public final static String KEY_CO2 = "C";
    public final static String KEY_SPO2 = "S";
    public final static String KEY_FRECUENCIA_CARDIACA = "HR";
    public final static ArrayList<String> KEYS = new ArrayList<String>(){
        {
            add(KEY_TEMPERATURA_SUJETO);
            add(KEY_SPO2);
            add(KEY_FRECUENCIA_CARDIACA);
            add(KEY_TEMPERATURA_AMBIENTE);
            add(KEY_CO2);
        }
    };

    public final static int DISPO_HABITACION = 1;
    public final static int DISPO_PERSONA = 2;

    public final static int TEMPERATURA_AMBIENTE = 1;
    public final static int TEMPERATURA_SUJETO = 2;
    public final static int CO2 = 3;
    public final static int SPO2 = 4;
    public final static int FRECUENCIA_CARDIACA = 5;

    public final static int MAP_ID = 8;
    public final static int SETTINGS_ID = 9;
    public final static int CALIBRATE_ID = 11;
    public final static int CORRELATION_ID = 12;
    public final static int[] IDS = new int[]{TEMPERATURA_SUJETO, TEMPERATURA_AMBIENTE, CO2, SPO2, DISPO_HABITACION, DISPO_PERSONA, DISPO_PERSONA, MAP_ID, SETTINGS_ID, FRECUENCIA_CARDIACA, CALIBRATE_ID, CORRELATION_ID};
    public final static int[] MEAS_IDS = new int[]{TEMPERATURA_SUJETO, TEMPERATURA_AMBIENTE, CO2, SPO2, FRECUENCIA_CARDIACA};

    public final static double TH_TEMP = 37.5;
    public final static double TH_SPO2 = 94;
    public final static double TH_CO2 = 800;
    public final static double TH_HR = 90;

    public static boolean CheckIdMeas(int id_meas){
        for(int id : IDS) {
            if (id == id_meas)
                return true;
        }
        return false;
    }

    public static boolean CheckKeyMeas(String id_meas){
        return KEYS.contains(id_meas);
    }

    public static int Key2Id(String key){
        switch (key){
            case KEY_TEMPERATURA_SUJETO:
                return TEMPERATURA_SUJETO;
            case KEY_TEMPERATURA_AMBIENTE:
                return TEMPERATURA_AMBIENTE;
            case KEY_CO2:
                return CO2;
            case KEY_SPO2:
                return SPO2;
            case KEY_FRECUENCIA_CARDIACA:
                return FRECUENCIA_CARDIACA;
            default:
                return -1;
        }
    }

    public static String Id2Key(int id) throws Exception {
        switch (id){
            case TEMPERATURA_SUJETO:
                return KEY_TEMPERATURA_SUJETO;
            case TEMPERATURA_AMBIENTE:
                return KEY_TEMPERATURA_AMBIENTE;
            case CO2:
                return KEY_CO2;
            case SPO2:
                return KEY_SPO2;
            case FRECUENCIA_CARDIACA:
                return KEY_FRECUENCIA_CARDIACA;
            default:
                throw new Exception("Invalid Id");
        }
    }

}
