package com.example.testapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.testapp.utils.DebugOverlayManager;

public class App extends Application {
    private static final String TAG = "App";
    private DebugOverlayManager debugOverlayManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(TAG, "未捕获的异常", ex);
                
                // 显示调试悬浮窗
                if (debugOverlayManager == null) {
                    debugOverlayManager = DebugOverlayManager.getInstance(getApplicationContext());
                }
                
                StringBuilder errorInfo = new StringBuilder();
                errorInfo.append("异常类型: ").append(ex.getClass().getSimpleName()).append("\n");
                errorInfo.append("异常信息: ").append(ex.getMessage()).append("\n");
                
                // 获取堆栈跟踪
                StackTraceElement[] stackTrace = ex.getStackTrace();
                if (stackTrace != null && stackTrace.length > 0) {
                    errorInfo.append("堆栈跟踪:\n");
                    for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
                        errorInfo.append("  at ").append(stackTrace[i].toString()).append("\n");
                    }
                }
                
                debugOverlayManager.updateDebugInfo(errorInfo.toString());
                debugOverlayManager.showDebugOverlay();
                
                // 调用默认异常处理器
                if (Thread.getDefaultUncaughtExceptionHandler() != null) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, ex);
                }
            }
        });
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}