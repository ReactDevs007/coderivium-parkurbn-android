package com.cruxlab.parkurbn.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

public class Spot implements Parcelable {

    private int type;
    private String licencePlateNumber;

    @SerializedName("human_readable_id")
    @Expose
    private String humanReadableId;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("is_taken")
    @Expose
    private boolean isTaken;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("loc")
    @Expose
    private Location loc;

    @SerializedName("pay_start_time")
    @Expose
    private int payStartTime;
    @SerializedName("pay_end_time")
    @Expose
    private int payEndTime;
    @SerializedName("price_per_minute")
    @Expose
    private double pricePerMin;

    @SerializedName("parking_time")
    @Expose
    private int parkingTime;

    @SerializedName("arrived_time")
    @Expose
    private int arrivedTime;
    @SerializedName("arrived_date")
    @Expose
    private String arrivedDate;

    private int expiration;
    private long clientArrivedMillis;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(licencePlateNumber);
        dest.writeString(humanReadableId);
        dest.writeString(address);
        dest.writeByte((byte) (isTaken ? 1 : 0));
        dest.writeString(id);
        dest.writeParcelable(loc, 0);
        dest.writeInt(payStartTime);
        dest.writeInt(payEndTime);
        dest.writeDouble(pricePerMin);
        dest.writeInt(parkingTime);
        dest.writeInt(arrivedTime);
        dest.writeString(arrivedDate);
        dest.writeInt(expiration);
        dest.writeLong(clientArrivedMillis);
    }

    protected Spot(Parcel in) {
        this.type = in.readInt();
        this.licencePlateNumber = in.readString();
        this.humanReadableId = in.readString();
        this.address = in.readString();
        this.isTaken = in.readByte() != 0;
        this.id = in.readString();
        this.loc = in.readParcelable(Location.class.getClassLoader());
        this.payStartTime = in.readInt();
        this.payEndTime = in.readInt();
        this.pricePerMin = in.readDouble();
        this.parkingTime = in.readInt();
        this.arrivedTime = in.readInt();
        this.arrivedDate = in.readString();
        this.expiration = in.readInt();
        this.clientArrivedMillis = in.readLong();
    }

    public Spot(Spot spot) {
        this.type = spot.getType();
        this.licencePlateNumber = spot.getLicencePlateNumber();
        this.humanReadableId = spot.getHumanReadableId();
        this.address = spot.getAddress();
        this.isTaken = spot.getIsTaken();
        this.id = spot.getId();
        this.loc = spot.getLoc();
        this.payStartTime = spot.getPayStartTime();
        this.payEndTime = spot.getPayEndTime();
        this.pricePerMin = spot.getPricePerMin();
        this.parkingTime = spot.getParkingTime();
        this.arrivedTime = spot.getArrivedTime();
        this.arrivedDate = spot.getArrivedDate();
        this.expiration = spot.getExpiration();
        this.clientArrivedMillis = spot.getClientArrivedMillis();
    }

    public static final Creator<Spot> CREATOR = new Creator<Spot>() {
        @Override
        public Spot createFromParcel(Parcel in) {
            return new Spot(in);
        }

        @Override
        public Spot[] newArray(int size) {
            return new Spot[size];
        }
    };

    public com.google.android.gms.maps.model.LatLng getLatLng() {
        return new com.google.android.gms.maps.model.LatLng(loc.getLat(), loc.getLng());
    }

    public void setLatLng(com.google.android.gms.maps.model.LatLng latLng) {
        this.loc = new Location(latLng.latitude, latLng.longitude);
    }

    public Location getLoc() {
        return loc;
    }

    public boolean isFree() {
        return !isTaken;
    }

    public double getLat() {
        return loc.getLat();
    }

    public double getLon() {
        return loc.getLng();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHumanReadableId() {
        return humanReadableId;
    }

    public void setHumanReadableId(String humanReadableId) {
        this.humanReadableId = humanReadableId;
    }

    public boolean getIsTaken() {
        return isTaken;
    }

    public void setIsTaken(boolean isTaken) {
        this.isTaken = isTaken;
    }

    public int getType() {
        return getHumanReadableId().length() % 3;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Spot spot = (Spot) o;

        return id.equals(spot.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getLicencePlateNumber() {
        return licencePlateNumber;
    }

    public void setLicencePlateNumber(String licencePlateNumber) {
        this.licencePlateNumber = licencePlateNumber;
    }

    public int getPayStartTime() {
        return payStartTime;
    }

    public void setPayStartTime(int payStartTime) {
        this.payStartTime = payStartTime;
    }

    public int getPayEndTime() {
        return payEndTime;
    }

    public void setPayEndTime(int payEndTime) {
        this.payEndTime = payEndTime;
    }

    public void setPayStartTime(Integer payStartTime) {
        this.payStartTime = payStartTime;
    }

    public void setPayEndTime(Integer payEndTime) {
        this.payEndTime = payEndTime;
    }

    public double getPricePerMin() {
        return pricePerMin;
    }

    public void setPricePerMin(double pricePerMin) {
        this.pricePerMin = pricePerMin;
    }

    public int getParkingTime() {
        return parkingTime;
    }

    public void setParkingTime(int parkingTime) {
        this.parkingTime = parkingTime;
    }

    public int getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(int arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public String getArrivedDate() {
        return arrivedDate;
    }

    public void setArrivedDate(String arrivedDate) {
        this.arrivedDate = arrivedDate;
    }

    public int getClientArrivedTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(clientArrivedMillis);
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public long getClientArrivedMillis() {
        return clientArrivedMillis;
    }

    public void setClientArrivedMillis(long clientArrivedMillis) {
        this.clientArrivedMillis = clientArrivedMillis;
    }

    public int getClientExpiration() {
        return getClientArrivedTime() + getParkingDurationMins();
    }

    public int getFreeMinsRemaining() {
        return (expiration == 0) ? Math.max(0, payStartTime - arrivedTime) : Math.max(0, payStartTime - expiration);
    }

    public int getParkingDurationMins() {
        return (expiration == 0) ? 0 : expiration - arrivedTime;
    }

    public int getParkingDurationSec() {
        return (expiration == 0) ? 0 : getParkingDurationMins() * 60;
    }

    public int getMaxParkingTimeMins() {
        return (expiration == 0) ? Math.max(0, payEndTime - arrivedTime) : Math.max(0, payEndTime - expiration);
    }

    public int getTimeRemainingMins() {
        return getTimeRemainingSec() / 60;
    }

    public int getTimeRemainingSec() {
        return (int) (getTimeRemainingMillis() / 1000);
    }

    public long getTimeRemainingMillis() {
        return clientArrivedMillis + getParkingDurationSec() * 1000L - System.currentTimeMillis();
    }

}

