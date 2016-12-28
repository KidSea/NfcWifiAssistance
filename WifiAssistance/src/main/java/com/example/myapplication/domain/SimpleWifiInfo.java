package com.example.myapplication.domain;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuxuehai on 16-12-28.
 */

public class SimpleWifiInfo implements Parcelable {

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    private String ssid;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;

    public SimpleWifiInfo(String type, String ssid, String key) {
        this.type = type;
        this.ssid = ssid;
        this.key = key;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(ssid);
        dest.writeString(key);
    }

    public boolean isKeyPreHashed(){
        return key != null && key.length() == 64;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SimpleWifiInfo> CREATOR = new Creator<SimpleWifiInfo>() {
        @Override
        public SimpleWifiInfo createFromParcel(Parcel in) {
            return new SimpleWifiInfo(in.readString(), in.readString(), in.readString());
        }

        @Override
        public SimpleWifiInfo[] newArray(int size) {
            return new SimpleWifiInfo[size];
        }
    };
}
