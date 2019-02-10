package com.cruxlab.parkurbn.model.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class PaymentRequest {

    private long amount;
    private String nonce;
    @SerializedName("license_plate_number")
    @Expose
    private String plateNumber;
    @SerializedName("spot_id")
    @Expose
    private String spotId;
    @SerializedName("arrived_time")
    @Expose
    private int arrivedTime;
    @SerializedName("arrived_date")
    @Expose
    private String arrivedDate;

    public PaymentRequest(long amount, String nonce, String plateNumber, String spotId, int arrivedTime, String arrivedDate) {
        this.amount = amount;
        this.nonce = nonce;
        this.plateNumber = plateNumber;
        this.spotId = spotId;
        this.arrivedTime = arrivedTime;
        this.arrivedDate = arrivedDate;
    }

}
