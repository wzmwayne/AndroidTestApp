package com.example.testapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.testapp.services.AppMonitorService;
import com.example.testapp.utils.PasswordManager;

public class MainActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    
    private CardView blacklistCard;
    private CardView whitelistCard;
    private TextView modeStatusText;
    private TextView currentModeText;
    private Button toggleModeButton;
    private Button settingsButton;
    
    private boolean isBlacklistMode = true; // 默认黑名单模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordManager = new PasswordManager(this);
        
        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        blacklistCard = findViewById(R.id.blacklistCard);
        whitelistCard = findViewById(R.id.whitelistCard);
        modeStatusText = findViewById(R.id.modeStatusText);
        currentModeText = findViewById(R.id.currentModeText);
        toggleModeButton = findViewById(R.id.toggleModeButton);
        settingsButton = findViewById(R.id.settingsButton);
    }

    private void setupClickListeners() {
        blacklistCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppListActivity.class);
            intent.putExtra("is_blacklist_mode", true);
            startActivity(intent);
        });

        whitelistCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AppListActivity.class);
            intent.putExtra("is_blacklist_mode", false);
            startActivity(intent);
        });

        toggleModeButton.setOnClickListener(v -> {
            isBlacklistMode = !isBlacklistMode;
            saveModePreference();
            updateUI();
            Toast.makeText(this, "已切换到" + (isBlacklistMode ? "黑名单" : "白名单") + "模式", Toast.LENGTH_SHORT).show();
        });

        settingsButton.setOnClickListener(v -> {
            // 打开设置界面
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void updateUI() {
        // 更新模式显示
        if (isBlacklistMode) {
            currentModeText.setText(R.string.blacklist_mode);
            modeStatusText.setText("黑名单模式：选中的应用将被拦截");
        } else {
            currentModeText.setText(R.string.whitelist_mode);
            modeStatusText.setText("白名单模式：只有选中的应用可以使用");
        }

        // 更新卡片状态
        updateCardStatus();
    }

    private void updateCardStatus() {
        String blacklistApps = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("blacklist_apps", "");
        String whitelistApps = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("whitelist_apps", "");

        int blacklistCount = blacklistApps.isEmpty() ? 0 : blacklistApps.split(",").length;
        int whitelistCount = whitelistApps.isEmpty() ? 0 : whitelistApps.split(",").length;

        // 更新黑名单卡片
        TextView blacklistCountText = blacklistCard.findViewById(R.id.countText);
        blacklistCountText.setText("已添加 " + blacklistCount + " 个应用");

        // 更新白名单卡片
        TextView whitelistCountText = whitelistCard.findViewById(R.id.whitelistCountText);
        whitelistCountText.setText("已添加 " + whitelistCount + " 个应用");

        // 高亮当前模式
        if (isBlacklistMode) {
            blacklistCard.setCardBackgroundColor(getResources().getColor(R.color.primary_color));
            whitelistCard.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        } else {
            blacklistCard.setCardBackgroundColor(getResources().getColor(R.color.card_background));
            whitelistCard.setCardBackgroundColor(getResources().getColor(R.color.primary_color));
        }
    }

    private void saveModePreference() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_blacklist_mode", isBlacklistMode)
                .apply();
    }

    private void loadModePreference() {
        isBlacklistMode = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_blacklist_mode", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModePreference();
        updateUI();
        
        // 暂时禁用监控服务启动，避免可能的闪退问题
        // startAppMonitorService();
    }
    
    private void startAppMonitorService() {
        try {
            boolean protectionEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getBoolean("protection_enabled", true);
                    
            if (protectionEnabled) {
                Intent serviceIntent = new Intent(this, AppMonitorService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 服务启动失败时不影响应用正常运行
        }
    }

    @Override
    public void onBackPressed() {
        // 返回键退出应用
        moveTaskToBack(true);
    }
}