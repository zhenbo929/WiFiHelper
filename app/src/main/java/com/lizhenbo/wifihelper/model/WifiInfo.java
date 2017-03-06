package com.lizhenbo.wifihelper.model;

public class WifiInfo {

    private String wifiName;
    private String wifiPwd;

    public String getWifiName() {
        return wifiName;
    }
    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }
    public String getWifiPwd() {
        return wifiPwd;
    }
    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }

    @Override
    public String toString() {
        return "WifiInfo{" +
                "wifiName='" + wifiName + '\'' +
                ", wifiPwd='" + wifiPwd + '\'' +
                '}';
    }
}
