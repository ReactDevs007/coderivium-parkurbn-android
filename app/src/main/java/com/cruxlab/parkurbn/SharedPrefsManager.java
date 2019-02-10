package com.cruxlab.parkurbn;

import android.content.Context;
import android.content.SharedPreferences;

import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.User;
import com.google.gson.Gson;

public class SharedPrefsManager {

    public static final String PREFERENCES_NAME = "ParkUrbnPreferences";
    private static final String KEY_USER = "user";
    private static final String KEY_CAPTURED_SPOT = "captured_spot";
    private static final String KEY_CAPTURED_SEGMENT = "captured_segment";
    private static final String KEY_PARKED_SPOT = "parked_spot";
    private static final String KEY_REMIND_BEFORE_TIME_MINS = "remind_before_time_mins";

    private static final SharedPrefsManager instance = new SharedPrefsManager();
    private SharedPreferences mSharedPrefs;

    public static SharedPrefsManager get() {
        return instance;
    }

    private SharedPrefsManager() {
        mSharedPrefs = ParkUrbnApplication.get().getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isUserLoggedIn() {
        return getUser() != null;
    }

    public User getUser() {
        String jsonUser = mSharedPrefs.getString(KEY_USER, null);
        return jsonUser == null ? null : new Gson().fromJson(jsonUser, User.class);
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        String jsonUser = new Gson().toJson(user);
        editor.putString(KEY_USER, jsonUser);
        editor.apply();
    }

    public void clearUser() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(KEY_USER, null);
        editor.apply();
    }

    public Spot getCapturedSpot() {
        String jsonSpot = mSharedPrefs.getString(KEY_CAPTURED_SPOT, null);
        return jsonSpot == null ? null : new Gson().fromJson(jsonSpot, Spot.class);
    }

    public void saveCapturedSpot(Spot spot) {
        clearCapturedSegment();
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        String jsonSpot = new Gson().toJson(spot);
        editor.putString(KEY_CAPTURED_SPOT, jsonSpot);
        editor.apply();
    }

    public void clearCapturedSpot() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(KEY_CAPTURED_SPOT, null);
        editor.apply();
    }

    public Segment getCapturedSegment() {
        String jsonSegment = mSharedPrefs.getString(KEY_CAPTURED_SEGMENT, null);
        return jsonSegment == null ? null : new Gson().fromJson(jsonSegment, Segment.class);
    }

    public void saveCapturedSegment(Segment segment) {
        clearCapturedSpot();
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        String jsonSegment = new Gson().toJson(segment);
        editor.putString(KEY_CAPTURED_SEGMENT, jsonSegment);
        editor.apply();
    }

    public void clearCapturedSegment() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(KEY_CAPTURED_SEGMENT, null);
        editor.apply();
    }

    public Spot getParkedSpot() {
        String jsonSpot = mSharedPrefs.getString(KEY_PARKED_SPOT, null);
        return jsonSpot == null ? null : new Gson().fromJson(jsonSpot, Spot.class);
    }

    public void saveParkedSpot(Spot spot) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        String jsonSpot = new Gson().toJson(spot);
        editor.putString(KEY_PARKED_SPOT, jsonSpot);
        editor.apply();
    }

    public void clearParkedSpot() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(KEY_PARKED_SPOT, null);
        editor.apply();
    }

    public int getRemindBeforeTimeMins() {
        return mSharedPrefs.getInt(KEY_REMIND_BEFORE_TIME_MINS, -1);
    }

    public void saveRemindBeforeTimeMins(int timeMins) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(KEY_REMIND_BEFORE_TIME_MINS, timeMins);
        editor.apply();
    }

    public void clearRemindBeforeTimeMins() {
        saveRemindBeforeTimeMins(-1);
    }

}
