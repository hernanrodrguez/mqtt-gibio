package com.example.mqttandroid;

import android.provider.BaseColumns;

public class DataC02Contract {

    public static abstract class DataC02Entry implements BaseColumns {
        public static final String TABLE_NAME ="DataC02";

        public static final String ID = "ID"; //AUTOINCREMENTAL
        public static final String CO2 = "C02";
        public static final String HORA = "hora";
        public static final String HABITACION = "habitacion";
    }

}
