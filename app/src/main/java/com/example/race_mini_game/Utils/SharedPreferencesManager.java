package com.example.race_mini_game.Utils;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager{

    private static volatile SharedPreferencesManager instance = null; //singletone class
    private static final String MEMORY_FILE = "MEMORY_FILE";
    private SharedPreferences sharedPref;

    private SharedPreferencesManager(Context context) {
        this.sharedPref = context.getSharedPreferences(MEMORY_FILE, Context.MODE_PRIVATE); //create share preferences object
    }

    public static void init(Context context) { //initialize function
        synchronized (SharedPreferencesManager.class) {
            if (instance == null) {
                instance = new SharedPreferencesManager(context);
            }
        }
    }

    public static SharedPreferencesManager getInstance() {
        return instance;
    }

    public void putInt(String key, int value) { //write int
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defaultValue) { //read int
        return sharedPref.getInt(key, defaultValue);
    }

    public void putString(String key, String value) { //write string
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) { //read string
        return sharedPref.getString(key, defaultValue);
    }
}