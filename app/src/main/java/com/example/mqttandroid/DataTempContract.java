package com.example.mqttandroid;

import android.provider.BaseColumns;

public class DataTempContract {
    /**
     * Esquema de la base de datos para datos de temp
     */

        public static abstract class DataTempEntry implements BaseColumns {
            public static final String TABLE_NAME ="DataTemp";

            public static final String TEMP = "temperatura";
            public static final String HORA = "hora";
            public static final String HABITACION = "habitacion";
        }



}
