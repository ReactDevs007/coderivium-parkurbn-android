package com.cruxlab.parkurbn.model.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateVehicleBody {

    private String nickname;
    @SerializedName("license_plate_number")
    @Expose
    private String licensePlateNumber;
    @SerializedName("default")
    @Expose
    private boolean isDefault;
    private String state;

    public CreateVehicleBody(String nickname, String licensePlateNumber, boolean isDefault, String state) {
        this.nickname = nickname;
        this.licensePlateNumber = licensePlateNumber;
        this.isDefault = isDefault;
        this.state = state;
    }

}
