package com.example.testapp;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp.model.AppInfo;
import com.example.testapp.adapter.AppListAdapter;
import com.example.testapp.utils.PermissionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends android.app.Activity {
    private TextView modeStatusText;
    private Button toggleModeButton;
    private Button showAppListButton;
    private Button showSettingsButton;
    private Button hideAppListButton;
    private Button hideSettingsButton;
    
    private LinearLayout mainLayout;
    private LinearLayout appListLayout;
    private LinearLayout settingsLayout;
    private EditText searchInput;
    private ProgressBar progressBar;
    private android.widget.ListView appListView;
    
    private boolean isBlacklistMode = true;
    private PackageManager packageManager;
    private List<AppInfo> allApps;
    private AppListAdapter adapter;
    private PermissionManager permissionManager;
    
    // 权限相关UI
    private TextView permissionStatusText;
    private Button requestPermissionsButton;
    
    // 设置相关
    private Switch protectionSwitch;
    private Switch autoStartSwitch;
    private Switch notificationSwitch;
    private Button changePasswordButton;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        packageManager = getPackageManager();
        permissionManager = new PermissionManager(this);
        
        createMainLayout();
        createAppListLayout();
        createSettingsLayout();
        
        setupClickListeners();
        loadModePreference();
        loadSettings();
        updateUI();
        updatePermissionStatus();
        
        // 默认显示主界面
        showMainView();
    }
    
    private void createMainLayout() {
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(16, 16, 16, 16);
        
        // 模式状态文本
        modeStatusText = new TextView(this);
        modeStatusText.setText("黑名单模式：选中的应用将被拦截");
        modeStatusText.setTextSize(18);
        modeStatusText.setPadding(0, 0, 0, 20);
        mainLayout.addView(modeStatusText);
        
        // 切换模式按钮
        toggleModeButton = new Button(this);
        toggleModeButton.setText("切换到白名单模式");
        mainLayout.addView(toggleModeButton);
        
        // 应用列表按钮
        showAppListButton = new Button(this);
        showAppListButton.setText("管理应用列表");
        mainLayout.addView(showAppListButton);
        
        // 设置按钮
        showSettingsButton = new Button(this);
        showSettingsButton.setText("设置");
        mainLayout.addView(showSettingsButton);
        
        // 权限状态文本
        permissionStatusText = new TextView(this);
        permissionStatusText.setTextSize(14);
        permissionStatusText.setPadding(0, 20, 0, 10);
        mainLayout.addView(permissionStatusText);
        
        // 请求权限按钮
        requestPermissionsButton = new Button(this);
        requestPermissionsButton.setText("请求必需权限");
        requestPermissionsButton.setBackgroundColor(0xFFFF6B6B);
        mainLayout.addView(requestPermissionsButton);
    }
    
    private void createAppListLayout() {
        appListLayout = new LinearLayout(this);
        appListLayout.setOrientation(LinearLayout.VERTICAL);
        appListLayout.setPadding(16, 16, 16, 16);
        appListLayout.setVisibility(View.GONE);
        
        // 标题和返回按钮
        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        hideAppListButton = new Button(this);
        hideAppListButton.setText("← 返回");
        titleLayout.addView(hideAppListButton);
        
        TextView titleText = new TextView(this);
        titleText.setText("管理应用列表");
        titleText.setTextSize(18);
        titleText.setPadding(20, 0, 0, 0);
        titleLayout.addView(titleText);
        
        appListLayout.addView(titleLayout);
        
        // 搜索框
        searchInput = new EditText(this);
        searchInput.setHint("搜索应用");
        appListLayout.addView(searchInput);
        
        // 进度条
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        appListLayout.addView(progressBar);
        
        // 应用列表
        appListView = new android.widget.ListView(this);
        appListLayout.addView(appListView);
        
        // 保存按钮
        Button saveButton = new Button(this);
        saveButton.setText("保存选择");
        appListLayout.addView(saveButton);
        
        saveButton.setOnClickListener(v -> saveSelectedApps());
    }
    
    private void createSettingsLayout() {
        settingsLayout = new LinearLayout(this);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        settingsLayout.setPadding(16, 16, 16, 16);
        settingsLayout.setVisibility(View.GONE);
        
        // 标题和返回按钮
        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        hideSettingsButton = new Button(this);
        hideSettingsButton.setText("← 返回");
        titleLayout.addView(hideSettingsButton);
        
        TextView titleText = new TextView(this);
        titleText.setText("设置");
        titleText.setTextSize(18);
        titleText.setPadding(20, 0, 0, 0);
        titleLayout.addView(titleText);
        
        settingsLayout.addView(titleLayout);
        
        // 基础设置
        TextView basicSettingsTitle = new TextView(this);
        basicSettingsTitle.setText("基础设置");
        basicSettingsTitle.setTextSize(16);
        basicSettingsTitle.setPadding(0, 20, 0, 10);
        settingsLayout.addView(basicSettingsTitle);
        
        // 启用保护
        createSwitchItem(settingsLayout, "启用保护", "开启应用拦截功能", protectionSwitch);
        
        // 自动启动
        createSwitchItem(settingsLayout, "自动启动", "开机自动启动保护", autoStartSwitch);
        
        // 显示通知
        createSwitchItem(settingsLayout, "显示通知", "拦截时显示通知", notificationSwitch);
        
        // 安全设置
        TextView securitySettingsTitle = new TextView(this);
        securitySettingsTitle.setText("安全设置");
        securitySettingsTitle.setTextSize(16);
        securitySettingsTitle.setPadding(0, 20, 0, 10);
        settingsLayout.addView(securitySettingsTitle);
        
        changePasswordButton = new Button(this);
        changePasswordButton.setText("修改密码");
        settingsLayout.addView(changePasswordButton);
        
        resetButton = new Button(this);
        resetButton.setText("重置所有设置");
        settingsLayout.addView(resetButton);
    }
    
    private void createSwitchItem(LinearLayout parent, String title, String summary, Switch switchView) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(0, 10, 0, 10);
        
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        itemLayout.addView(titleText);
        
        TextView summaryText = new TextView(this);
        summaryText.setText(summary);
        summaryText.setTextSize(12);
        summaryText.setTextColor(0xFF808080);
        itemLayout.addView(summaryText);
        
        if (switchView == null) {
            switchView = new Switch(this);
        }
        
        LinearLayout switchLayout = new LinearLayout(this);
        switchLayout.setOrientation(LinearLayout.HORIZONTAL);
        switchLayout.setGravity(android.view.Gravity.END);
        switchLayout.addView(switchView);
        
        itemLayout.addView(switchLayout);
        parent.addView(itemLayout);
    }

    private void setupClickListeners() {
        toggleModeButton.setOnClickListener(v -> {
            isBlacklistMode = !isBlacklistMode;
            saveModePreference();
            updateUI();
        });
        
        showAppListButton.setOnClickListener(v -> {
            showAppListView();
            loadApps();
        });
        
        hideAppListButton.setOnClickListener(v -> showMainView());
        
        showSettingsButton.setOnClickListener(v -> showSettingsView());
        
        hideSettingsButton.setOnClickListener(v -> showMainView());
        
        requestPermissionsButton.setOnClickListener(v -> requestAllPermissions());
        
        changePasswordButton.setOnClickListener(v -> {
            // 重置密码
            getSharedPreferences("password_prefs", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "密码已重置", Toast.LENGTH_SHORT).show();
        });

        resetButton.setOnClickListener(v -> {
            // 重置所有设置
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "已重置所有设置", Toast.LENGTH_SHORT).show();
            loadSettings();
        });

        protectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("protection_enabled", isChecked)
                    .apply();
        });

        autoStartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("auto_start_enabled", isChecked)
                    .apply();
        });

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("notification_enabled", isChecked)
                    .apply();
        });
        
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void showMainView() {
        mainLayout.setVisibility(View.VISIBLE);
        appListLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.GONE);
    }
    
    private void showAppListView() {
        mainLayout.setVisibility(View.GONE);
        appListLayout.setVisibility(View.VISIBLE);
        settingsLayout.setVisibility(View.GONE);
    }
    
    private void showSettingsView() {
        mainLayout.setVisibility(View.GONE);
        appListLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.VISIBLE);
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
    
    private void updatePermissionStatus() {
        boolean hasAllPermissions = permissionManager.hasAllPermissions();
        
        if (hasAllPermissions) {
            permissionStatusText.setText("权限状态：所有必需权限已授予 ✓");
            permissionStatusText.setTextColor(0xFF4CAF50);
            requestPermissionsButton.setVisibility(View.GONE);
        } else {
            StringBuilder missingPermissions = new StringBuilder("权限状态：缺少必需权限：\n");
            
            if (!permissionManager.hasOverlayPermission()) {
                missingPermissions.append("• 悬浮窗权限\n");
            }
            if (!permissionManager.hasUsageStatsPermission()) {
                missingPermissions.append("• 使用情况访问权限\n");
            }
            if (!permissionManager.hasBatteryOptimizationPermission()) {
                missingPermissions.append("• 电池优化豁免权限\n");
            }
            
            permissionStatusText.setText(missingPermissions.toString());
            permissionStatusText.setTextColor(0xFFFF6B6B);
            requestPermissionsButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void requestAllPermissions() {
        if (!permissionManager.hasOverlayPermission()) {
            Intent intent = permissionManager.getOverlayPermissionIntent();
            if (intent != null) {
                startActivity(intent);
                return;
            }
        }
        
        if (!permissionManager.hasUsageStatsPermission()) {
            Intent intent = permissionManager.getUsageStatsPermissionIntent();
            if (intent != null) {
                startActivity(intent);
                return;
            }
        }
        
        if (!permissionManager.hasBatteryOptimizationPermission()) {
            Intent intent = permissionManager.getBatteryOptimizationPermissionIntent();
            if (intent != null) {
                startActivity(intent);
                return;
            }
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
    
    private void loadSettings() {
        boolean protectionEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("protection_enabled", true);
        boolean autoStartEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("auto_start_enabled", true);
        boolean notificationEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("notification_enabled", true);

        protectionSwitch.setChecked(protectionEnabled);
        autoStartSwitch.setChecked(autoStartEnabled);
        notificationSwitch.setChecked(notificationEnabled);
    }
    
    private void loadApps() {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            List<AppInfo> apps = new ArrayList<>();
            List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            
            for (ApplicationInfo appInfo : installedApps) {
                // 排除本应用
                if (appInfo.packageName.equals(getPackageName())) {
                    continue;
                }
                
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                Drawable icon = packageManager.getApplicationIcon(appInfo);
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                AppInfo app = new AppInfo(appInfo.packageName, appName, icon, isSystemApp);
                apps.add(app);
            }
            
            // 按应用名排序
            Collections.sort(apps, Comparator.comparing(AppInfo::getAppName, String.CASE_INSENSITIVE_ORDER));
            
            runOnUiThread(() -> {
                allApps = apps;
                adapter = new AppListAdapter(this, apps);
                appListView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }
    
    private void filterApps(String query) {
        if (adapter == null) return;
        
        List<AppInfo> filteredApps = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (app.getAppName().toLowerCase().contains(query.toLowerCase())) {
                filteredApps.add(app);
            }
        }
        
        adapter.updateList(filteredApps);
    }
    
    private void saveSelectedApps() {
        if (adapter == null) return;
        
        List<AppInfo> selectedApps = adapter.getSelectedApps();
        
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "请至少选择一个应用", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 保存选中的应用到SharedPreferences
        StringBuilder selectedPackages = new StringBuilder();
        for (AppInfo app : selectedApps) {
            if (selectedPackages.length() > 0) {
                selectedPackages.append(",");
            }
            selectedPackages.append(app.getPackageName());
        }
        
        String prefKey = isBlacklistMode ? "blacklist_apps" : "whitelist_apps";
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString(prefKey, selectedPackages.toString())
                .apply();
        
        Toast.makeText(this, "已保存 " + selectedApps.size() + " 个应用", Toast.LENGTH_SHORT).show();
        showMainView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModePreference();
        loadSettings();
        updateUI();
        updatePermissionStatus();
    }

    @Override
    public void onBackPressed() {
        if (appListLayout.getVisibility() == View.VISIBLE) {
            showMainView();
        } else if (settingsLayout.getVisibility() == View.VISIBLE) {
            showMainView();
        } else {
            finish();
        }
    }
}