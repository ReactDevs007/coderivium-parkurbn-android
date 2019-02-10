package com.cruxlab.parkurbn;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.cruxlab.parkurbn.tools.Converter;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.List;

public class DurationPickerManager {

    private Context mContext;
    private DurationPickerCallback mCallback;
    private WheelView.WheelViewStyle mStyle;
    private WheelView<String> mWvHours, mWvMins;
    private List<String> mHoursList, mMinsList;
    private int duration, maxMins, maxHours, maxHourMins;
    private boolean isHoursInited, isMinsInited;

    public interface DurationPickerCallback {

        void onDurationPicked(int hours, int mins);

    }

    public DurationPickerManager(Context context, WheelView<String> wvHours, WheelView<String> wvMins, int maxTimeMins, DurationPickerCallback callback) {
        mContext = context;
        mCallback = callback;
        mWvMins = wvMins;
        mWvHours = wvHours;
        maxMins = maxTimeMins;
        maxHours = maxMins / 60;
        maxHourMins = maxMins % 60;
        mHoursList = getWheelHoursList(maxHours);
        mMinsList = getWheelMinsList(0, maxHours, maxHourMins);

        initStyle();
        initWheelView(wvHours, mHoursList, R.string.hours, mStyle);
        initWheelView(wvMins, mMinsList, R.string.min, mStyle);

        wvHours.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                if (!isHoursInited) {
                    isHoursInited = true;
                    mWvHours.setSelection(0);
                    mWvHours.setVisibility(View.VISIBLE);
                } else {
                    int hours = Integer.parseInt(s);
                    int minsPos = mWvMins.getCurrentPosition();
                    if (duration / 60 == 0) minsPos++;
                    if (hours == 0) minsPos--;
                    mMinsList = getWheelMinsList(hours, maxHours, maxHourMins);
                    minsPos = Math.min(minsPos, mMinsList.size() - 1);
                    minsPos = Math.max(minsPos, 0);
                    mWvMins.setListAndSelection(mMinsList, minsPos);
                    int mins = Integer.parseInt(mMinsList.get(minsPos));
                    duration = hours * 60 + mins;
                    if (mCallback != null) {
                        mCallback.onDurationPicked(hours, mins);
                    }
                }
            }
        });
        wvMins.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                if (!isMinsInited) {
                    isMinsInited = true;
                    mWvMins.setSelection(Math.min(14, mMinsList.size() - 1));
                    mWvMins.setVisibility(View.VISIBLE);
                    duration = Math.min(15, mMinsList.size());
                } else {
                    int hours = Integer.parseInt(mWvHours.getSelectionItem());
                    int mins = Integer.parseInt(s);
                    duration = hours * 60 + mins;
                }
                if (mCallback != null) {
                    mCallback.onDurationPicked(duration / 60, duration % 60);
                }
            }
        });
    }

    public int getDuration() {
        return duration;
    }

    public void setMaxTime(int maxTimeMins) {
        if (mWvHours == null || mWvMins == null) return;
        maxMins = maxTimeMins;
        maxHours = maxMins / 60;
        maxHourMins = maxMins % 60;
        mHoursList = getWheelHoursList(maxHours);
        mMinsList = getWheelMinsList(0, maxHours, maxHourMins);
        mWvHours.setListAndSelection(mHoursList, 0);
        mWvMins.setListAndSelection(mMinsList, Math.min(14, mMinsList.size() - 1));
    }

    private void initStyle() {
        mStyle = new WheelView.WheelViewStyle();
        mStyle.selectedTextColor = Color.BLACK;
        mStyle.holoBorderColor = Color.parseColor("#aaafb7");
        mStyle.textColor = Color.GRAY;
        mStyle.textSize = 16;
        mStyle.selectedTextSize = 20;
    }

    private void initWheelView(WheelView wheelView, List<String> itemList, int extraTextResId, WheelView.WheelViewStyle style) {
        wheelView.setWheelAdapter(new ArrayWheelAdapter(mContext));
        wheelView.setSkin(WheelView.Skin.Holo);
        wheelView.setWheelData(itemList);
        wheelView.setExtraText(mContext.getString(extraTextResId), Color.BLACK, Converter.dpToPx(12), Converter.dpToPx(40));
        wheelView.setStyle(style);
    }

    private List<String> getWheelMinsList(int hours, int maxHours, int maxLastHourMins) {
        List<String> list = new ArrayList<>();
        int start = 0, end = 59;
        if (hours == 0) start = 1;
        if (hours == maxHours) end = maxLastHourMins;
        for (int i = start; i <= end; i++) {
            list.add((i < 10 ? "0" : "") + String.valueOf(i));
        }
        return list;
    }

    private List<String> getWheelHoursList(int maxHours) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i <= maxHours; i++) {
            list.add((i < 10 ? "0" : "") + String.valueOf(i));
        }
        return list;
    }

}
