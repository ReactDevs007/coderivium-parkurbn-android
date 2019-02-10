package com.cruxlab.parkurbn.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vehicle implements Parcelable {

    @SerializedName("default")
    @Expose
    private boolean isDefault = false;
    @SerializedName("license_plate_number")
    @Expose
    private String licensePlateNumber;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("nickname")
    @Expose
    private String nickname;
    @SerializedName("state")
    @Expose
    private String state;

    public Vehicle(String nickname, String licensePlateNumber, String state) {
        this.nickname = nickname;
        this.licensePlateNumber = licensePlateNumber;
        this.state = state;
    }

    protected Vehicle(Parcel in) {
        isDefault = in.readByte() != 0;
        licensePlateNumber = in.readString();
        id = in.readString();
        nickname = in.readString();
        state = in.readString();
    }

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public void setLicensePlateNumber(String licensePlateNumber) {
        this.licensePlateNumber = licensePlateNumber;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isDefault ? 1 : 0));
        dest.writeString(licensePlateNumber);
        dest.writeString(id);
        dest.writeString(nickname);
        dest.writeString(state);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
