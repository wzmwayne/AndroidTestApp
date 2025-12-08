package com.example.testapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PasswordManager {
    private static final String PREFS_NAME = "secure_prefs";
    private static final String PASSWORD_KEY = "app_password";
    private static final String FIRST_TIME_KEY = "first_time";
    
    private SharedPreferences sharedPreferences;
    private Context context;
    
    public PasswordManager(Context context) {
        this.context = context;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // 降级到普通SharedPreferences
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }
    
    public boolean isFirstTime() {
        return sharedPreferences.getBoolean(FIRST_TIME_KEY, true);
    }
    
    public void setFirstTimeComplete() {
        sharedPreferences.edit().putBoolean(FIRST_TIME_KEY, false).apply();
    }
    
    public boolean hasPassword() {
        return sharedPreferences.contains(PASSWORD_KEY);
    }
    
    public void setPassword(String password) {
        sharedPreferences.edit().putString(PASSWORD_KEY, password).apply();
    }
    
    public boolean checkPassword(String password) {
        String savedPassword = sharedPreferences.getString(PASSWORD_KEY, "");
        return savedPassword.equals(password);
    }
    
    public boolean isValidPassword(String password) {
        return password != null && password.length() == 6 && password.matches("\\d{6}");
    }
}