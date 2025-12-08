package com.example.testapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {
    private List<AppInfo> appList;
    private List<AppInfo> filteredAppList;
    private Context context;
    private OnAppClickListener listener;

    public interface OnAppClickListener {
        void onAppClick(AppInfo appInfo);
    }

    public AppListAdapter(Context context, OnAppClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.appList = new ArrayList<>();
        this.filteredAppList = new ArrayList<>();
    }

    public void setAppList(List<AppInfo> appList) {
        this.appList = appList;
        this.filteredAppList = new ArrayList<>(appList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredAppList.clear();
        if (query == null || query.isEmpty()) {
            filteredAppList.addAll(appList);
        } else {
            String searchQuery = query.toLowerCase();
            for (AppInfo app : appList) {
                if (app.getAppName().toLowerCase().contains(searchQuery) ||
                    app.getPackageName().toLowerCase().contains(searchQuery)) {
                    filteredAppList.add(app);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo appInfo = filteredAppList.get(position);
        
        holder.appName.setText(appInfo.getAppName());
        holder.packageName.setText(appInfo.getPackageName());
        holder.icon.setImageDrawable(appInfo.getIcon());
        holder.checkBox.setChecked(appInfo.isSelected());
        
        // 系统应用显示不同颜色
        if (appInfo.isSystemApp()) {
            holder.packageName.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.packageName.setTextColor(context.getResources().getColor(R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredAppList.size();
    }

    public List<AppInfo> getSelectedApps() {
        List<AppInfo> selectedApps = new ArrayList<>();
        for (AppInfo app : appList) {
            if (app.isSelected()) {
                selectedApps.add(app);
            }
        }
        return selectedApps;
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView appName;
        TextView packageName;
        CheckBox checkBox;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            packageName = itemView.findViewById(R.id.packageName);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}