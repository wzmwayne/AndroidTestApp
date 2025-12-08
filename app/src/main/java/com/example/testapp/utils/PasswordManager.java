package com.example.testapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PasswordManager {
    private static final String PREFS_NAME = "app_prefs";
    private static final String FIRST_TIME_KEY = "first_time";
    
    private SharedPreferences sharedPreferences;
    private Context context;
    
    public PasswordManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public boolean isFirstTime() {
        return sharedPreferences.getBoolean(FIRST_TIME_KEY, true);
    }
    
    public void setFirstTimeComplete() {
        sharedPreferences.edit().putBoolean(FIRST_TIME_KEY, false).apply();
    }
}