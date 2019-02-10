package com.cruxlab.parkurbn.model.request;

import com.cruxlab.parkurbn.model.Location;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SpotsRequest {

    private Location position;
    private int quantity;
    @SerializedName("parking_time")
    @Expose
    private int parkingTime;

    public SpotsRequest(Location position, int quantity, int minTime) {
        this.position = position;
        this.quantity = quantity;
        this.parkingTime = minTime;
    }

}
