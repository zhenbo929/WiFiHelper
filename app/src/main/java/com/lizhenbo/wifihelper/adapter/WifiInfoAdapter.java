package com.lizhenbo.wifihelper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lizhenbo.wifihelper.R;
import com.lizhenbo.wifihelper.model.WifiInfo;

import java.text.MessageFormat;
import java.util.List;

public class WifiInfoAdapter extends BaseAdapter {

    private Context mContext = null;
    private List<WifiInfo> mWiFiInfoList = null;
    private LayoutInflater mInflater = null;

    public WifiInfoAdapter(Context context, List<WifiInfo> dataList) {
        this.mContext = context;
        this.mWiFiInfoList = dataList;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mWiFiInfoList != null ? mWiFiInfoList.size() : 0;
    }

    @Override
    public WifiInfo getItem(int position) {
        return mWiFiInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.wifi_info_item, null);
            viewHolder.tvWifiName = (TextView) convertView.findViewById(R.id.tv_wifi_name);
            viewHolder.tvWifiPwd = (TextView) convertView.findViewById(R.id.tv_wifi_pwd);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        /*
        WiFi密码助手
        功能：通过读取系统配置文件，来获取WiFi系统保存的WiFi密码（必须获取root权限）
        应用：通过WiFi万能钥匙连接WiFi后，通过此应用来读取WiFi密码，便于分享和电脑连接
        PS：解决了WiFi名字中包含中文不能显示的问题
*/
        WifiInfo data = mWiFiInfoList.get(position);

        viewHolder.tvWifiName.setText(MessageFormat.format(mContext.getString(R.string.wifi_name), data.getWifiName()));
        viewHolder.tvWifiPwd.setText(MessageFormat.format(mContext.getString(R.string.wifi_pwd), data.getWifiPwd()));

        return convertView;
    }

    private class ViewHolder {
        TextView tvWifiName, tvWifiPwd;
    }
}
