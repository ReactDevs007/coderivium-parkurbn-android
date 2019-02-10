package com.cruxlab.parkurbn.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location implements Parcelable {

    @SerializedName("lat")
    @Expose
    private double lat;
    @SerializedName("lng")
    @Expose
    private double lng;

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Location(com.google.android.gms.maps.model.LatLng latLng) {
        this.lat = latLng.latitude;
        this.lng = latLng.longitude;
    }

    public Location(com.google.maps.model.LatLng latLng) {
        this.lat = latLng.lat;
        this.lng = latLng.lng;
    }

    public Location(android.location.Location loc) {
        this.lat = loc.getLatitude();
        this.lng = loc.getLongitude();
    }

    protected Location(Parcel in) {
        this.lat = in.readDouble();
        this.lng = in.readDouble();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    @Override
    public String toString() {
        return String.valueOf(lat) + ',' + String.valueOf(lng);
    }
}
