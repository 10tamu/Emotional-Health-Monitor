package com.swin.emotionalhealthmonitor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {

    public static final String SKIP_ROWS = "skipRows";
    public static final String READ_FREQUENCY = "readFrequency";
    public static final String HR_MAX_Y_AXIS = "hrMaxYAxis";
    public static final String HR_MIN_Y_AXIS = "hrMinYAxis";
    public static final String SC_MAX_Y_AXIS = "scMaxYAxis";
    public static final String SC_MIN_Y_AXIS = "scMinYAxis";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public MySharedPreferences(Context context) {
        pref = context.getSharedPreferences("ehm", 0);
        editor = pref.edit();
    }

    public SharedPreferences getSharedPreferences() {
        return pref;
    }

    public void writeData(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }

    public  String readData(String key){

         String val;

        if(key.equals(HR_MIN_Y_AXIS)) {
            val = pref.getString(key, "65");
        }
        else if(key.equals(HR_MAX_Y_AXIS)) {
            val = pref.getString(key, "85");
        }
        else if(key.equals(SC_MIN_Y_AXIS)) {
            val = pref.getString(key, "7");
        }
        else if(key.equals(SC_MAX_Y_AXIS)) {
            val = pref.getString(key, "13");
        }
        else if(key.equals(READ_FREQUENCY)) {
            val = pref.getString(key, "30");
        }
        else {
            val = pref.getString(key, "0");
        }

        return val;
    }

    public void setToDefault(String key) {
        if(key.equals(HR_MIN_Y_AXIS)) {
            writeData(key, "65");
        }
        else if(key.equals(HR_MAX_Y_AXIS)) {
            writeData(key, "85");
        }
        else if(key.equals(SC_MIN_Y_AXIS)) {
            writeData(key, "7");
        }
        else if(key.equals(SC_MAX_Y_AXIS)) {
            writeData(key, "13");
        }
        else if(key.equals(READ_FREQUENCY)) {
            writeData(key, "30");
        }
    }
}
