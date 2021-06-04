package com.example.mqttandroid;

import java.util.ArrayList;

public final class Constants {

    public final static String BROKER_KEY = "Broker";
    public final static String TOPIC_KEY = "Topic";
    public final static String DATA_KEY = "Data";
    public final static String CASE_KEY = "Case";
    public final static String QUANT_KEY = "Quant";

    public final static String TEMP_OBJ_KEY = "TO";
    public final static String TEMP_AMB_KEY = "TA";
    public final static String CO2_KEY = "C";
    public final static String SPO2_KEY = "S";
    public final static String HR_KEY = "HR";
    public final static ArrayList<String> KEYS = new ArrayList<String>(){
        {
            add(TEMP_OBJ_KEY);
            add(TEMP_AMB_KEY);
            add(CO2_KEY);
            add(SPO2_KEY);
            add(HR_KEY);
        }
    };

    public final static int TEMP_OBJ_ID = 1;
    public final static int TEMP_AMB_ID = 2;
    public final static int CO2_ID = 3;
    public final static int SPO2_ID = 4;
    public final static int ROOMS_ID = 5;
    public final static int PERSON_ID = 6;
    public final static int PEOPLE_ID = 7;
    public final static int MAP_ID = 8;
    public final static int SETTINGS_ID = 9;
    public final static int HR_ID = 10;
    public final static int CALIBRATE_ID = 11;
    public final static int[] IDS = new int[]{TEMP_OBJ_ID, TEMP_AMB_ID, CO2_ID, SPO2_ID, ROOMS_ID, PERSON_ID, PEOPLE_ID, MAP_ID, SETTINGS_ID, HR_ID, CALIBRATE_ID};
    public final static int[] MEAS_IDS = new int[]{TEMP_OBJ_ID, TEMP_AMB_ID, CO2_ID, SPO2_ID, HR_ID};

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

    public static int Key2Id(String key) throws Exception {
        switch (key){
            case TEMP_OBJ_KEY:
                return TEMP_OBJ_ID;
            case TEMP_AMB_KEY:
                return TEMP_AMB_ID;
            case CO2_KEY:
                return CO2_ID;
            case SPO2_KEY:
                return SPO2_ID;
            case HR_KEY:
                return HR_ID;
            default:
                throw new Exception("Invalid Key");
        }
    }

    public static String Id2Key(int id) throws Exception {
        switch (id){
            case TEMP_OBJ_ID:
                return TEMP_OBJ_KEY;
            case TEMP_AMB_ID:
                return TEMP_AMB_KEY;
            case CO2_ID:
                return CO2_KEY;
            case SPO2_ID:
                return SPO2_KEY;
            case HR_ID:
                return HR_KEY;
            default:
                throw new Exception("Invalid Id");
        }
    }

}
