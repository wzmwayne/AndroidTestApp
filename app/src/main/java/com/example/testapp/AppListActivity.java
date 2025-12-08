package com.example.testapp;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapter.AppListAdapter;
import com.example.testapp.model.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListActivity extends AppCompatActivity implements AppListAdapter.OnAppClickListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText searchInput;
    private TextView titleText;
    private AppListAdapter adapter;
    private List<AppInfo> allApps;
    private PackageManager packageManager;
    private boolean isBlacklistMode = true; // 默认黑名单模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        packageManager = getPackageManager();
        
        initViews();
        setupRecyclerView();
        loadApps();
        setupSearch();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        searchInput = findViewById(R.id.searchInput);
        titleText = findViewById(R.id.titleText);
        
        // 获取传入的模式参数
        isBlacklistMode = getIntent().getBooleanExtra("is_blacklist_mode", true);
        titleText.setText(isBlacklistMode ? R.string.blacklist_mode : R.string.whitelist_mode);
        
        // 返回按钮
        findViewById(android.R.id.home).setOnClickListener(v -> finish());
        
        // 保存按钮
        findViewById(R.id.saveButton).setOnClickListener(v -> saveSelectedApps());
    }

    private void setupRecyclerView() {
        adapter = new AppListAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
                adapter.setAppList(allApps);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    @Override
    public void onAppClick(AppInfo appInfo) {
        appInfo.setSelected(!appInfo.isSelected());
        adapter.notifyDataSetChanged();
    }

    private void saveSelectedApps() {
        List<AppInfo> selectedApps = adapter.getSelectedApps();
        
        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "请至少选择一个应用", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 保存选中的应用到SharedPreferences
        // 这里简化处理，实际应用中应该有专门的数据管理
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
        finish();
    }
}