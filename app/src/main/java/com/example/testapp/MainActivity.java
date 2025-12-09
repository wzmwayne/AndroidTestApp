package com.example.testapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.testapp.services.AppMonitorService;
import com.example.testapp.utils.PasswordManager;
import com.example.testapp.utils.DebugOverlayManager;
import com.example.testapp.utils.DebugClickDetector;

public class MainActivity extends AppCompatActivity {
    private PasswordManager passwordManager;
    private DebugOverlayManager debugOverlayManager;
    
    private CardView blacklistCard;
    private CardView whitelistCard;
    private TextView modeStatusText;
    private TextView currentModeText;
    private Button toggleModeButton;
    private Button settingsButton;
    
    private boolean isBlacklistMode = true; // 默认黑名单模式
    
    // 左上角连击检测
    private long lastTouchTime = 0;
    private int touchCount = 0;
    private static final int REQUIRED_TOUCHES = 5;
    private static final long TOUCH_TIMEOUT = 2000; // 2秒内

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordManager = new PasswordManager(this);
        debugOverlayManager = DebugOverlayManager.getInstance(this);
        
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
            blacklistCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
            whitelistCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
        } else {
            blacklistCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
            whitelistCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_color));
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 检测左上角连击
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            float x = ev.getX();
            float y = ev.getY();
            
            // 检查是否在左上角区域（100x100像素）
            if (x <= 100 && y <= 100) {
                long currentTime = System.currentTimeMillis();
                
                if (currentTime - lastTouchTime > TOUCH_TIMEOUT) {
                    touchCount = 1;
                } else {
                    touchCount++;
                }
                
                lastTouchTime = currentTime;
                
                if (touchCount >= REQUIRED_TOUCHES) {
                    // 显示调试悬浮窗
                    debugOverlayManager.showDebugOverlay();
                    Toast.makeText(this, "调试面板已开启", Toast.LENGTH_SHORT).show();
                    touchCount = 0;
                }
            }
        }
        
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // 返回键退出应用
        moveTaskToBack(true);
    }
}