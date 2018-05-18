package com.example.maksymg.nearme;

import android.bluetooth.BluetoothAdapter;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("name")
    private String mName;
    @SerializedName("lat")
    private double mLat;
    @SerializedName("lon")
    private double mLon;

    private static User instance;

    public static User getInstance() {
        if(instance == null)
            return new User();
        return instance;
    }

    private User(){
        mName = BluetoothAdapter.getDefaultAdapter().getName();
    }

    public String getName() {
        return mName;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setLat(double lat) {
        this.mLat = lat;
    }

    public void setLon(double lon) {
        mLon = lon;
    }
}
