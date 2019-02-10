package com.cruxlab.parkurbn.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;

import com.cruxlab.parkurbn.NotificationPublisher;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.tools.DialogUtils;

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    public IntentFilter mFilter;
    public NotificationPublisher mReceiver;

    /* LIFECYCLE */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mFilter = new IntentFilter();
        mFilter.addAction("OPEN_PARKING_SOON_DIALOG");
        mReceiver = new NotificationPublisher() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onNotificationReceived();
            }
        };
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onDestroy() {
        hideLoader();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        try {
            if (mReceiver != null) {
                unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {
            mReceiver = null;
        }
        super.onStop();
    }

    /* END LIFECYCLE */

    protected void onNotificationReceived() {
        DialogUtils.showParkingMeterExpiresSoonDialog(this, new DialogUtils.ConfirmDialogCallback() {
            @Override
            public void okButtonPressed() {
                Intent intent = new Intent(BaseActivity.this, ParkingInfoActivity.class);
                intent.putExtra(BundleArguments.FROM, ParkingInfoActivity.FROM_FRAGMENT_NOTIFICATION);
                startActivity(intent);
            }

            @Override
            public void cancelButtonPressed() {

            }
        });
    }

    public void showLoader() {
        showLoader(getString(R.string.loading));
    }

    public void showLoader(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(BaseActivity.this);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage("\n   " + message + "\n");
        mProgressDialog.show();
    }

    public void hideLoader() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public boolean isLoaderShown() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

}
