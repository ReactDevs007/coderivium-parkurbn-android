package com.cruxlab.parkurbn.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Segment implements Parcelable {

    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("price_per_hour")
    @Expose
    private double pricePerHour;
    @SerializedName("free_spots")
    @Expose
    private int freeSpots;
    @SerializedName("loc")
    @Expose
    private Location loc;
    @SerializedName("total_spots")
    @Expose
    private int totalSpots;
    @SerializedName("geometry")
    @Expose
    private List<Location> geometry;

    @SerializedName("parking_time")
    @Expose
    private int parkingTime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(id);
        dest.writeDouble(pricePerHour);
        dest.writeInt(freeSpots);
        dest.writeValue(loc);
        dest.writeInt(totalSpots);
        dest.writeTypedList(geometry);
        dest.writeInt(parkingTime);
    }

    protected Segment(Parcel in) {
        this.address = in.readString();
        this.id = in.readString();
        this.pricePerHour = in.readDouble();
        this.freeSpots = in.readInt();
        this.loc = (Location) in.readValue(Location.class.getClassLoader());
        this.totalSpots = in.readInt();
        this.geometry = new ArrayList<>();
        in.readTypedList(this.geometry, Location.CREATOR);
        this.parkingTime = in.readInt();
    }

    public static final Creator<Segment> CREATOR = new Creator<Segment>() {
        @Override
        public Segment createFromParcel(Parcel in) {
            return new Segment(in);
        }

        @Override
        public Segment[] newArray(int size) {
            return new Segment[size];
        }
    };

    public com.google.android.gms.maps.model.LatLng getCentralSpot() {
        return new com.google.android.gms.maps.model.LatLng(loc.getLat(), loc.getLng());
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFreeSpots() {
        return freeSpots;
    }

    public void setFreeSpots(int freeSpots) {
        this.freeSpots = freeSpots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public int getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(int totalSpots) {
        this.totalSpots = totalSpots;
    }

    public List<com.google.android.gms.maps.model.LatLng> getGeometry() {
        List<com.google.android.gms.maps.model.LatLng> latLngs = new ArrayList<>();
        for (Location location : geometry) {
            latLngs.add(new com.google.android.gms.maps.model.LatLng(location.getLat(), location.getLng()));
        }
        return latLngs;
    }

    public void setGeometry(List<Location> geometry) {
        this.geometry = geometry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Segment segment = (Segment) o;

        return id.equals(segment.id);
    }

    public int getParkingTime() {
        return parkingTime;
    }

    public void setParkingTime(int parkingTime) {
        this.parkingTime = parkingTime;
    }
}
