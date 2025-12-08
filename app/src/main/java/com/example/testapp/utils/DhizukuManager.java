package com.example.testapp.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;

public class DhizukuManager {
    private static final String TAG = "DhizukuManager";
    private Context context;
    private DhizukuPermissionListener listener;

    public interface DhizukuPermissionListener {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public DhizukuManager(Context context) {
        this.context = context;
    }

    public void setPermissionListener(DhizukuPermissionListener listener) {
        this.listener = listener;
    }

    public boolean isDhizukuAvailable() {
        return Dhizuku.isInit();
    }

    public boolean hasPermission() {
        return Dhizuku.hasPermission();
    }

    public void requestPermission() {
        if (!isDhizukuAvailable()) {
            Log.d(TAG, "Dhizuku is not available");
            if (listener != null) {
                listener.onPermissionDenied();
            }
            return;
        }

        if (hasPermission()) {
            Log.d(TAG, "Dhizuku permission already granted");
            if (listener != null) {
                listener.onPermissionGranted();
            }
            return;
        }

        Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
            @Override
            public void onRequestPermission(String packageName, int requestCode) {
                Log.d(TAG, "Requesting Dhizuku permission");
            }

            @Override
            public void onRequestPermissionResult(boolean isGranted, int requestCode) {
                Log.d(TAG, "Dhizuku permission result: " + isGranted);
                if (listener != null) {
                    if (isGranted) {
                        listener.onPermissionGranted();
                    } else {
                        listener.onPermissionDenied();
                    }
                }
            }
        });
    }

    public Intent getDhizukuSettingsIntent() {
        try {
            return new Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                    .setData(android.net.Uri.parse("package:com.rosan.dhizuku"));
        } catch (Exception e) {
            Log.e(TAG, "Error creating Dhizuku settings intent", e);
            return null;
        }
    }

    // 使用Dhizuku执行需要高权限的操作
    public void executeWithPermission(DhizukuAction action) {
        if (!hasPermission()) {
            Log.d(TAG, "Dhizuku permission not granted");
            return;
        }

        try {
            Dhizuku.send(context, action.getIntent(), result -> {
                if (result != null) {
                    action.onResult(result);
                } else {
                    action.onError(new Exception("Dhizuku operation failed"));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error executing Dhizuku action", e);
            action.onError(e);
        }
    }

    public interface DhizukuAction {
        Intent getIntent();
        void onResult(Object result);
        void onError(Exception e);
    }
}