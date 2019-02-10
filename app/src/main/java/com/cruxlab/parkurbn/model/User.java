package com.cruxlab.parkurbn.model;

import com.google.gson.annotations.SerializedName;

public class User {

    private String id;
    private String email;
    private Vehicle vehicle;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("fb_id")
    private String fbId;
    @SerializedName("receive_receipts")
    private boolean receiveReceipts;

    public User() {}

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFbId() {
        return fbId;
    }

    public boolean isReceiveReceipts() {
        return receiveReceipts;
    }

    public void setReceiveReceipts(boolean receiveReceipts) {
        this.receiveReceipts = receiveReceipts;
    }

    public boolean isLoggedViaFb() {
        return fbId != null;
    }

}

