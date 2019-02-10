package com.cruxlab.parkurbn.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alla on 6/19/17.
 */

@Entity
public class History {

    @ColumnInfo(name = "amount")
    private double amount;

    @ColumnInfo(name = "card_number")
    @SerializedName("card_number")
    private String card;

    @ColumnInfo(name = "paypal_email")
    @SerializedName("paypal_email")
    private String paypalEmail;

    @PrimaryKey
    private String date;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "map_url")
    @SerializedName("map_url")
    private String mapUrl;

    @ColumnInfo(name = "start_parking_time")
    @SerializedName("start_parking_time")
    private int startParkingTime;

    @ColumnInfo(name = "end_parking_time")
    @SerializedName("end_parking_time")
    private int endParkingTime;

    @ColumnInfo(name = "parking_date")
    @SerializedName("parking_date")
    private String parkingDate;

    public double getAmount() {
        return amount;
    }

    public String getCard() {
        return card;
    }

    public String getPaypalEmail() {
        return paypalEmail;
    }

    public String getDate() {
        return date;
    }

    public String getAddress() {
        return address;
    }

    public String getMapUrl() {
        return mapUrl;
    }

    public int getStartParkingTime() {
        return startParkingTime;
    }

    public void setStartParkingTime(int startParkingTime) {
        this.startParkingTime = startParkingTime;
    }

    public int getEndParkingTime() {
        return endParkingTime;
    }

    public void setEndParkingTime(int endParkingTime) {
        this.endParkingTime = endParkingTime;
    }

    public String getParkingDate() {
        return parkingDate;
    }

    public void setParkingDate(String parkingDate) {
        this.parkingDate = parkingDate;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void setPaypalEmail(String paypalEmail) {
        this.paypalEmail = paypalEmail;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }
}
