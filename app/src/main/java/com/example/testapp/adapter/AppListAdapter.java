package com.example.testapp.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testapp.R;
import com.example.testapp.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends android.widget.BaseAdapter {
    private List<AppInfo> appList;
    private Context context;
    private LayoutInflater inflater;

    public AppListAdapter(Context context, List<AppInfo> appList) {
        this.context = context;
        this.appList = new ArrayList<>(appList);
        this.inflater = LayoutInflater.from(context);
    }

    public void updateList(List<AppInfo> newList) {
        this.appList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_app, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.appIcon);
            holder.appName = convertView.findViewById(R.id.appName);
            holder.packageName = convertView.findViewById(R.id.packageName);
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInfo appInfo = appList.get(position);
        
        holder.appName.setText(appInfo.getAppName());
        holder.packageName.setText(appInfo.getPackageName());
        holder.icon.setImageDrawable(appInfo.getIcon());
        holder.checkBox.setChecked(appInfo.isSelected());
        
        // 点击事件
        convertView.setOnClickListener(v -> {
            appInfo.setSelected(!appInfo.isSelected());
            holder.checkBox.setChecked(appInfo.isSelected());
        });
        
        holder.checkBox.setOnClickListener(v -> {
            appInfo.setSelected(!appInfo.isSelected());
            holder.checkBox.setChecked(appInfo.isSelected());
        });

        return convertView;
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

    private static class ViewHolder {
        ImageView icon;
        TextView appName;
        TextView packageName;
        CheckBox checkBox;
    }
}