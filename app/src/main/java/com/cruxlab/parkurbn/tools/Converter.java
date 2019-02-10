package com.cruxlab.parkurbn.tools;

import android.content.res.Resources;

import java.util.Locale;

public abstract class Converter {

    private static String[] title = { "Alabama", "Alaska", "Arizona", "Arkansas", "Wyoming", "Washington", "Vermont", "Virginia", "Wisconsin", "Hawaii", "Delaware", "Georgia", "West Virginia", "Illinois", "Indiana", "California", "Kansas", "Kentucky", "Colorado", "Connecticut", "Louisiana", "Massachusetts", "Minnesota", "Mississippi", "Missouri", "Michigan", "Montana", "Maine", "Maryland", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New York", "New Mexico", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island and Providence Plantations", "North Dakota", "North Carolina", "Tennessee", "Texas", "Florida", "South Dakota", "South Carolina", "Utah"};
    private static String[] id = {"AL", "AK", "AZ", "AR", "WY", "WA", "VT", "VA", "WI", "HI", "DE", "GA", "WV", "IL", "IN", "CA", "KS", "KY", "CO", "CT", "LA", "MA", "MN", "MS", "MO", "MI", "MT", "ME", "MD", "NE", "NV", "NH", "NJ", "NY", "NM", "OH", "OK", "OR", "PA", "RI", "ND", "NC", "TN", "TX", "FL", "SD", "SC", "UT"};

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static String minsToTimeAMPM(int timeMins) {
        int h = timeMins / 60;
        String t;
        if (h % 12 == 0) {
            if (h != 12) t = "AM";
            else t = "PM";
            h = 12;
        } else {
            t = h < 12 ? "AM" : "PM";
            h %= 12;
        }
        int m = timeMins % 60;
        return String.format(Locale.US, "%02d", h) + ':' + String.format(Locale.US, "%02d", m) + " " + t;
    }

    public static String minsToDuration(int mins) {
        return mins / 60 + " h " + mins % 60 + " m";
    }

    public static String minsToTime(int timeMins) {
        int hours = timeMins / 60;
        int mins = timeMins % 60;
        return (hours < 10 ? "0" : "") + hours + ":" + (mins < 10 ? "0" : "") + mins;
    }

    //Use map?
    public static String stateIdToTitle(String stateId) {
        for (int i = 0; i < id.length; i++) {
            if (id[i].equals(stateId)) {
                return title[i];
            }
        }
        return "";
    }

    public static String getPriceStr(double price) {
        return "$" + String.format(Locale.US, "%.2f", price);
    }
}
