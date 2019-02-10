package com.cruxlab.parkurbn.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.*;

/**
 * Created by alla on 5/4/17.
 */

public class PlaceSuggestion implements SearchResult {

    private String mPlaceId;
    private String mDescription1;
    private String mDescription2;

    public PlaceSuggestion(String placeId, String description1, String description2) {
        this.mPlaceId = placeId;
        this.mDescription1 = description1;
        this.mDescription2 = description2;
    }

    public PlaceSuggestion(Parcel parcel) {
        mPlaceId = parcel.readString();
        mDescription1 = parcel.readString();
        mDescription2 = parcel.readString();
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    @Override
    public String getText1() {
        return mDescription1;
    }

    @Override
    public String getText2() {
        return mDescription2;
    }

    public static final Parcelable.Creator<PlaceSuggestion> CREATOR = new Parcelable.Creator<PlaceSuggestion>() {
        @Override
        public PlaceSuggestion createFromParcel(Parcel in) {
            return new PlaceSuggestion(in);
        }

        @Override
        public PlaceSuggestion[] newArray(int size) {
            return new PlaceSuggestion[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mPlaceId);
        parcel.writeString(mDescription1);
        parcel.writeString(mDescription2);
    }
}
