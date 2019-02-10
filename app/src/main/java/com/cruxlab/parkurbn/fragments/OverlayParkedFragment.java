package com.cruxlab.parkurbn.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.activities.ParkingInfoActivity;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Spot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OverlayParkedFragment extends BaseOverlayFragment {

    @BindView(R.id.ll_address)
    ViewGroup llAddress;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.pb_time_left)
    ProgressBar pbTimeLeft;
    @BindView(R.id.ibtn_my_location)
    ImageButton ibtnMyLocation;

    @BindColor(R.color.color_green)
    int colorGreen;
    @BindColor(R.color.color_orange)
    int colorOrange;
    @BindColor(R.color.color_red)
    int colorRed;

    private static final long TIMER_STEP = 1000L;

    private CountDownTimer mCountDownTimer;
    private Spot mSpot;

    public static OverlayParkedFragment newInstance() {
        return new OverlayParkedFragment();
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overlay_parked, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mSpot = SharedPrefsManager.get().getParkedSpot();
        tvAddress.setText(mSpot.getAddress());
        getMapActivity().showLoader();
        requestMap();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSpot = SharedPrefsManager.get().getParkedSpot();
        setupTimer();
        if (mMapHolder != null && mMapHolder.isMapReady()) {
            mMapHolder.showParkingTimeMarker(mSpot);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    /* END LIFECYCLE */
    /* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {
        mMapHolder = mapHolder;
        mMapHolder.setMinZoom(ZoomLevels.DEFAULT_MIN);
        initCompass();
        mMapHolder.setPadding(llAddress.getHeight() + pbTimeLeft.getHeight());
        mMapHolder.setZoom(ZoomLevels.DEFAULT, mSpot.getLatLng(), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                getMapActivity().hideLoader();
            }

            @Override
            public void onCancel() {
                onFinish();
            }
        });
        mMapHolder.showParkingTimeMarker(mSpot);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mUnbinder != null) {
            showInfo();
        }
        return true;
    }

    @Override
    public void onMyLocationCentered(boolean centered) {
        if (mUnbinder != null) {
            ibtnMyLocation.setVisibility(!centered ? View.VISIBLE : View.GONE);
        }
    }

    /* END MAP MANAGER */
    /* ON CLICK */

    @OnClick(R.id.ll_address)
    void showInfo() {
        Intent intent = new Intent(getActivity(), ParkingInfoActivity.class);
        intent.putExtra(BundleArguments.FROM, ParkingInfoActivity.FROM_FRAGMENT);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.slide_bottom_in, R.anim.slide_top_out);
        startActivity(intent, options.toBundle());
    }

    @OnClick(R.id.ibtn_my_location)
    void showMyLocation() {
        requestGPSLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(Location location) {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.setMyLocationButtonEnabled();
                }
            }
        });
    }

    /* END ON CLICK */

    private void setupTimer() {
        pbTimeLeft.setMax(mSpot.getParkingDurationSec());
        updateProgress(Math.max(0, mSpot.getTimeRemainingSec()));
        if (mSpot.getTimeRemainingSec() > 0) {
            mCountDownTimer = new CountDownTimer(mSpot.getTimeRemainingMillis(), TIMER_STEP) {

                @Override
                public void onTick(long millisUntilFinished) {
                    if (mUnbinder != null) {
                        updateProgress((int) (millisUntilFinished / 1000));
                    }
                }

                @Override
                public void onFinish() {
                    if (mUnbinder != null) {
                        updateProgress(0);
                    }
                }
            }.start();
        }
    }

    private void updateProgress(int timePassed) {
        int duration = mSpot.getParkingDurationSec();
        float duration25 = duration * 0.25f;
        float duration50 = duration * 0.5f;
        ColorStateList stateList = null;
        if (timePassed >= 0 && timePassed < duration25) {
            stateList = ColorStateList.valueOf(colorRed);
        } else if (timePassed >= duration25 && timePassed < duration50) {
            stateList = ColorStateList.valueOf(colorOrange);
        } else if (timePassed >= duration50) {
            stateList = ColorStateList.valueOf(colorGreen);
        }
        pbTimeLeft.setProgressTintList(stateList);
        pbTimeLeft.setProgress(timePassed);
    }


}
