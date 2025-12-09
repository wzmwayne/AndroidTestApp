package com.example.testapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends android.app.Activity {
    private TextView modeStatusText;
    private Button toggleModeButton;
    private Button settingsButton;
    private Button appListButton;
    
    private boolean isBlacklistMode = true; // 默认黑名单模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.activity_list_item);
        
        // 创建简单的布局
        createSimpleLayout();
        
        setupClickListeners();
        updateUI();
    }
    
    private void createSimpleLayout() {
        // 创建主布局
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(16, 16, 16, 16);
        
        // 模式状态文本
        modeStatusText = new android.widget.TextView(this);
        modeStatusText.setText("黑名单模式：选中的应用将被拦截");
        modeStatusText.setTextSize(18);
        modeStatusText.setPadding(0, 0, 0, 20);
        mainLayout.addView(modeStatusText);
        
        // 切换模式按钮
        toggleModeButton = new android.widget.Button(this);
        toggleModeButton.setText("切换到白名单模式");
        toggleModeButton.setOnClickListener(v -> {
            isBlacklistMode = !isBlacklistMode;
            saveModePreference();
            updateUI();
        });
        mainLayout.addView(toggleModeButton);
        
        // 应用列表按钮
        appListButton = new android.widget.Button(this);
        appListButton.setText("管理应用列表");
        appListButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, AppListActivity.class);
            intent.putExtra("is_blacklist_mode", isBlacklistMode);
            startActivity(intent);
        });
        mainLayout.addView(appListButton);
        
        // 设置按钮
        settingsButton = new android.widget.Button(this);
        settingsButton.setText("设置");
        settingsButton.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
        });
        mainLayout.addView(settingsButton);
        
        setContentView(mainLayout);
    }

    private void setupClickListeners() {
        // 点击事件已在createSimpleLayout中设置
    }

    private void updateUI() {
        if (isBlacklistMode) {
            modeStatusText.setText("黑名单模式：选中的应用将被拦截");
            toggleModeButton.setText("切换到白名单模式");
        } else {
            modeStatusText.setText("白名单模式：只有选中的应用可以使用");
            toggleModeButton.setText("切换到黑名单模式");
        }
    }

    private void saveModePreference() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_blacklist_mode", isBlacklistMode)
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModePreference();
        updateUI();
    }
    
    private void loadModePreference() {
        isBlacklistMode = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_blacklist_mode", true);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}