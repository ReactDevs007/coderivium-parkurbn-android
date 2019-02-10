package com.cruxlab.parkurbn;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.cruxlab.parkurbn.activities.ParkingInfoActivity;
import com.cruxlab.parkurbn.api.LoadHistoryManager;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.db.ParkUrbnDatabase;
import com.google.maps.GeoApiContext;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class ParkUrbnApplication extends Application {

    private static ParkUrbnApplication app;
    private ParkUrbnApi mParkUrbnApi;
    private GeoApiContext mGeoApiContext;
    private ParkUrbnDatabase mDb;
    private LoadHistoryManager mLoadHistoryManager;

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans_Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        app = this;
    }

    public static ParkUrbnApplication get() {
        return app;
    }

    public ParkUrbnApi getParkUrbnApi() {
        if (mParkUrbnApi == null) {
            mParkUrbnApi = ParkUrbnApi.Api.create();
        }
        return mParkUrbnApi;
    }

    public ParkUrbnDatabase getDb() {
        if (mDb == null) {
            mDb = ParkUrbnDatabase.getInMemoryDatabase(getApplicationContext());
        }
        return mDb;
    }

    public GeoApiContext getGeoApiContext() {
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext().setApiKey(getString(R.string.google_maps_key));
        }
        return mGeoApiContext;
    }

    public LoadHistoryManager getLoadHistoryManager() {
        if (mLoadHistoryManager == null) {
            mLoadHistoryManager = LoadHistoryManager.get();
        }
        return mLoadHistoryManager;
    }

    public static String isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    List<ActivityManager.AppTask > taskInfo = activityManager.getAppTasks();
                    return taskInfo.get(0).getTaskInfo().topActivity.getClassName();
                } else {
                    List<ActivityManager.RunningTaskInfo > taskInfo = activityManager.getRunningTasks(1);
                    return taskInfo.get(0).topActivity.getClassName();
                }
            }
        }
        return null;
    }
}
