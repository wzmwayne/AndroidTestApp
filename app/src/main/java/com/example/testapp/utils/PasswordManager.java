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
            try {
                sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            } catch (Exception ex) {
                // 如果仍然失败，创建一个新的SharedPreferences实例
                ex.printStackTrace();
                try {
                    sharedPreferences = context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE);
                } catch (Exception fallbackEx) {
                    // 最后的保障，使用应用默认的SharedPreferences
                    fallbackEx.printStackTrace();
                    sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME + "_emergency", Context.MODE_PRIVATE);
                }
            }
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
        try {
            // 使用密码的平方进行加密存储，使用BigInteger防止溢出
            java.math.BigInteger passwordValue = new java.math.BigInteger(password);
            java.math.BigInteger squaredPassword = passwordValue.multiply(passwordValue);
            sharedPreferences.edit().putString(PASSWORD_KEY, squaredPassword.toString()).apply();
        } catch (NumberFormatException e) {
            // 如果转换失败，使用原始方法
            try {
                sharedPreferences.edit().putString(PASSWORD_KEY, password).apply();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("设置密码失败", ex);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("设置密码失败", e);
        }
    }
    
    public boolean checkPassword(String password) {
        try {
            String savedPassword = sharedPreferences.getString(PASSWORD_KEY, "");
            // 使用BigInteger防止溢出
            java.math.BigInteger savedPasswordValue = new java.math.BigInteger(savedPassword);
            java.math.BigInteger inputPasswordValue = new java.math.BigInteger(password);
            java.math.BigInteger squaredInputPassword = inputPasswordValue.multiply(inputPasswordValue);
            return savedPasswordValue.equals(squaredInputPassword);
        } catch (NumberFormatException e) {
            // 如果转换失败，使用原始比较方法
            String savedPassword = sharedPreferences.getString(PASSWORD_KEY, "");
            return savedPassword.equals(password);
        }
    }
    
    public boolean isValidPassword(String password) {
        return password != null && password.length() == 6 && password.matches("\\d{6}");
    }
}