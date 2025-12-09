package com.example.testapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
    private LinearLayout errorLayout;
    private EditText searchInput;
    private ProgressBar progressBar;
    private android.widget.ListView appListView;
    
    // 错误界面相关
    private TextView errorText;
    private Button copyErrorButton;
    private Button restartButton;
    private String currentError = "";
    
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
        
        try {
            packageManager = getPackageManager();
            permissionManager = new PermissionManager(this);
            
            // 创建根布局
            LinearLayout rootLayout = new LinearLayout(this);
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            
            createMainLayout();
            createAppListLayout();
            createSettingsLayout();
            createErrorLayout();
            
            // 将所有子布局添加到根布局
            rootLayout.addView(mainLayout);
            rootLayout.addView(appListLayout);
            rootLayout.addView(settingsLayout);
            rootLayout.addView(errorLayout);
            
            // 设置根布局为内容视图
            setContentView(rootLayout);
            
            setupClickListeners();
            loadModePreference();
            loadSettings();
            updateUI();
            updatePermissionStatus();
            
            // 默认显示主界面
            showMainView();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorScreen("应用启动出错：" + e.getMessage());
        }
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
        
        // 保存按钮（移到顶部）
        Button saveButton = new Button(this);
        saveButton.setText("保存选择");
        saveButton.setBackgroundColor(0xFF4CAF50);
        appListLayout.addView(saveButton);
        
        // 搜索框
        searchInput = new EditText(this);
        searchInput.setHint("搜索用户安装的应用");
        appListLayout.addView(searchInput);
        
        // 进度条
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        appListLayout.addView(progressBar);
        
        // 应用列表
        appListView = new android.widget.ListView(this);
        appListLayout.addView(appListView);
        
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
        
        // 初始化Switch控件
        protectionSwitch = new Switch(this);
        autoStartSwitch = new Switch(this);
        notificationSwitch = new Switch(this);
        
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
    
    private void createErrorLayout() {
        errorLayout = new LinearLayout(this);
        errorLayout.setOrientation(LinearLayout.VERTICAL);
        errorLayout.setPadding(16, 16, 16, 16);
        errorLayout.setVisibility(View.GONE);
        
        // 错误标题
        TextView errorTitle = new TextView(this);
        errorTitle.setText("发生错误");
        errorTitle.setTextSize(20);
        errorTitle.setPadding(0, 0, 0, 20);
        errorLayout.addView(errorTitle);
        
        // 错误信息文本框
        errorText = new TextView(this);
        errorText.setTextSize(14);
        errorText.setBackgroundColor(0xFFFFF0F0);
        errorText.setPadding(16, 16, 16, 16);
        errorText.setTextColor(0xFFD32F2F);
        errorText.setMaxHeight(300);
        errorLayout.addView(errorText);
        
        // 按钮容器
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setPadding(0, 20, 0, 0);
        
        // 复制错误按钮
        copyErrorButton = new Button(this);
        copyErrorButton.setText("复制错误信息");
        buttonLayout.addView(copyErrorButton);
        
        // 重启按钮
        restartButton = new Button(this);
        restartButton.setText("重启应用");
        buttonLayout.addView(restartButton);
        
        errorLayout.addView(buttonLayout);
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
        
        // 错误界面按钮点击事件
        copyErrorButton.setOnClickListener(v -> copyErrorToClipboard());
        restartButton.setOnClickListener(v -> restartApp());
        
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

        if (protectionSwitch != null) {
            protectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("protection_enabled", isChecked)
                        .apply();
            });
        }

        if (autoStartSwitch != null) {
            autoStartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("auto_start_enabled", isChecked)
                        .apply();
            });
        }

        if (notificationSwitch != null) {
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("notification_enabled", isChecked)
                        .apply();
            });
        }
        
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
        try {
            if (mainLayout != null) mainLayout.setVisibility(View.VISIBLE);
            if (appListLayout != null) appListLayout.setVisibility(View.GONE);
            if (settingsLayout != null) settingsLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAppListView() {
        try {
            if (mainLayout != null) mainLayout.setVisibility(View.GONE);
            if (appListLayout != null) appListLayout.setVisibility(View.VISIBLE);
            if (settingsLayout != null) settingsLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showSettingsView() {
        try {
            if (mainLayout != null) mainLayout.setVisibility(View.GONE);
            if (appListLayout != null) appListLayout.setVisibility(View.GONE);
            if (settingsLayout != null) settingsLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showErrorScreen(String errorMessage) {
        currentError = errorMessage;
        try {
            if (mainLayout != null) mainLayout.setVisibility(View.GONE);
            if (appListLayout != null) appListLayout.setVisibility(View.GONE);
            if (settingsLayout != null) settingsLayout.setVisibility(View.GONE);
            if (errorLayout != null) {
                errorLayout.setVisibility(View.VISIBLE);
                if (errorText != null) {
                    errorText.setText(errorMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void copyErrorToClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("错误信息", currentError);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "错误信息已复制到剪贴板", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "复制失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void restartApp() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
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
        try {
            if (permissionManager == null) {
                permissionManager = new PermissionManager(this);
            }
            
            boolean hasAllPermissions = permissionManager.hasAllPermissions();
            
            if (permissionStatusText != null && requestPermissionsButton != null) {
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
                    if (!permissionManager.hasAccessibilityPermission()) {
                        missingPermissions.append("• 无障碍服务权限\n");
                    }
                    
                    permissionStatusText.setText(missingPermissions.toString());
                    permissionStatusText.setTextColor(0xFFFF6B6B);
                    requestPermissionsButton.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorScreen("权限检查出错：" + e.getMessage());
        }
    }
    
    private void requestAllPermissions() {
        try {
            if (permissionManager == null) {
                permissionManager = new PermissionManager(this);
            }
            
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
            
            if (!permissionManager.hasAccessibilityPermission()) {
                Intent intent = permissionManager.getAccessibilityPermissionIntent();
                if (intent != null) {
                    startActivity(intent);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorScreen("打开权限设置出错：" + e.getMessage());
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
        try {
            boolean protectionEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getBoolean("protection_enabled", true);
            boolean autoStartEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getBoolean("auto_start_enabled", true);
            boolean notificationEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getBoolean("notification_enabled", true);

            if (protectionSwitch != null) protectionSwitch.setChecked(protectionEnabled);
            if (autoStartSwitch != null) autoStartSwitch.setChecked(autoStartEnabled);
            if (notificationSwitch != null) notificationSwitch.setChecked(notificationEnabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadApps() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        new Thread(() -> {
            try {
                List<AppInfo> apps = new ArrayList<>();
                if (packageManager != null) {
                    List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                    
                    if (installedApps != null) {
                        for (ApplicationInfo appInfo : installedApps) {
                            try {
                                // 排除本应用
                                if (getPackageName() != null && getPackageName().equals(appInfo.packageName)) {
                                    continue;
                                }
                                
                                // 只显示用户安装的应用，排除系统应用
                                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                    continue;
                                }
                                
                                String appName = packageManager.getApplicationLabel(appInfo).toString();
                                Drawable icon = packageManager.getApplicationIcon(appInfo);
                                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                                
                                AppInfo app = new AppInfo(appInfo.packageName, appName, icon, isSystemApp);
                                apps.add(app);
                            } catch (Exception e) {
                                // 跳过有问题的应用
                                continue;
                            }
                        }
                    }
                }
                
                // 按应用名排序
                Collections.sort(apps, Comparator.comparing(AppInfo::getAppName, String.CASE_INSENSITIVE_ORDER));
                
                runOnUiThread(() -> {
                    allApps = apps;
                    try {
                        adapter = new AppListAdapter(this, apps);
                        if (appListView != null) {
                            appListView.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "加载应用列表出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    e.printStackTrace();
                    Toast.makeText(this, "加载应用列表出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
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
        String savedString = selectedPackages.toString();
        
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString(prefKey, savedString)
                .apply();
        
        // 验证保存是否成功
        String verifyString = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString(prefKey, "");
        
        android.util.Log.d("MainActivity", "保存模式: " + (isBlacklistMode ? "黑名单" : "白名单"));
        android.util.Log.d("MainActivity", "保存内容: " + savedString);
        android.util.Log.d("MainActivity", "验证内容: " + verifyString);
        android.util.Log.d("MainActivity", "保存是否成功: " + savedString.equals(verifyString));
        
        Toast.makeText(this, "已保存 " + selectedApps.size() + " 个应用", Toast.LENGTH_SHORT).show();
        showMainView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadModePreference();
            loadSettings();
            updateUI();
            updatePermissionStatus();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorScreen("应用恢复出错：" + e.getMessage());
        }
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