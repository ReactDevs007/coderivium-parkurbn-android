package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.MapActivity;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.interfaces.MapManager;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.tools.Converter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.GeoApiContext;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public abstract class BaseOverlayFragment extends BaseFragment implements MapManager, Animation.AnimationListener {

    @BindView(R.id.ibtn_compass)
    ImageButton mCompass;

    private static final int FADE_ANIM_TIME = 500;
    private static final int TIMER_DELAY = 1500;

    protected ParkUrbnApi mParkUrbnApi;
    protected MapHolder mMapHolder;
    protected GeoApiContext mGeoApiContext;
    private Timer mCompassTimer;

    protected boolean bottomAnimInProgress;

    protected LocationRequestedCallback mLocationRequestedCallback;

    /* INTERFACES */

    protected interface LogoAnimationCallback {
        void onPaddingSet();
    }

    protected interface LocationRequestedCallback {
        void onLocationGranted(Location location);
    }

    /* END INTERFACES */
    /* LIFECYCLE */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGeoApiContext = ParkUrbnApplication.get().getGeoApiContext();
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        mCompassTimer = new Timer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMapHolder != null) {
            mMapHolder.resetPrefs();
            mMapHolder = null;
        }
    }

    /* END LIFECYCLE */
    /* ANIM LISTENER */

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    /* END ANIM LISTENER */
    /* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {

    }

    @Override
    public void onZoomChanged(float zoom) {

    }

    @Override
    public void onLocationPermitted() {
        getMapActivity().showLoader(getString(R.string.determining_your_location));
    }

    @Override
    public void onLocationUnavailable() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.unable_to_determine_your_current_location, Toast.LENGTH_SHORT).show();
                getMapActivity().hideLoader();
            }
        });
    }

    @Override
    public void onLocationGranted(final Location location) {
        triggerLocationCallback(location);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onBearingChanged(float bearing) {
        if (mUnbinder != null) {
            if (Math.abs(bearing) == 0f) {
                mCompassTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mUnbinder != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mUnbinder != null) {
                                        mCompass.animate().alpha(0f).setDuration(FADE_ANIM_TIME).start();
                                    }
                                }
                            });
                        }
                    }
                }, TIMER_DELAY);
            } else {
                if (mCompassTimer != null) mCompassTimer.cancel();
                mCompassTimer = new Timer();
                mCompass.setAlpha(1f);
                mCompass.setVisibility(View.VISIBLE);
            }
            mCompass.animate().rotation(-bearing).setDuration(0).start();
        }
    }

    @Override
    public void onFreeSpotCntChanged(int freeCnt) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMyLocationCentered(boolean centered) {

    }

    @Override
    public void onSpotSelected(Spot spot) {

    }

    @Override
    public void onSegmentSelected(Segment segment) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onSpotTaken(String id) {

    }

    @Override
    public void onSegmentChanged(String id, int count) {

    }

    @Override
    public void onCameraStoppedMoving(LatLng center) {

    }

    @Override
    public void onSpotNoLongerSatisfyFilter(String id) {

    }

    @Override
    public void onSegmentNoLongerSatisfyFilter(String id) {

    }

    @Override
    public void onSpotUnavailable(String id) {

    }

    @Override
    public void onSegmentUnavailable(String id) {

    }

    @Override
    public void onBackPressed() {
        getMapActivity().superOnBackPressed();
    }

    /* END MAP MANAGER */
    /* ON CLICK */

    @OnClick(R.id.ibtn_compass)
    void rotateToNorth() {
        mMapHolder.setBearing(0);
    }

    @Optional
    @OnClick(R.id.ibtn_nav_btn)
    void navBtnClick() {
        openDrawer();
    }

    @Optional
    @OnClick(R.id.ibtn_back_btn)
    void backBtnClick() {
        onBackPressed();
    }

    @Optional
    @OnClick(R.id.ll_bottom)
    void preventOnMapClick() {}

    /* END ON CLICK */

    protected void requestMap() {
        getMapActivity().requestMap(this);
    }

    protected Animation getAnimBottomToTop() {
        return getAnimBottomToTop(null);
    }

    protected Animation getAnimBottomToTop(Animation.AnimationListener animationListener) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_to_top);
        animation.setAnimationListener(animationListener);
        return animation;
    }

    protected Animation getAnimTopToBottom() {
        return getAnimTopToBottom(null);
    }

    protected Animation getAnimTopToBottom(Animation.AnimationListener animationListener) {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.top_to_bottom);
        animation.setAnimationListener(animationListener);
        return animation;
    }

    protected MapActivity getMapActivity() {
        return (MapActivity) getActivity();
    }

    protected void openDrawer() {
        getMapActivity().openDrawer();
    }

    protected void initCompass() {
        if (mMapHolder.getBearing() == 0f) mCompass.setAlpha(0f);
        else onBearingChanged(mMapHolder.getBearing());
    }

    protected void animateGoogleLogo(final int targetPadding) {
        animateGoogleLogo(targetPadding, null);
    }

    protected void animateGoogleLogo(final int targetPadding, final LogoAnimationCallback callback) {
        final long start = SystemClock.uptimeMillis();
        final Handler animHandler = new Handler();
        final int val = Converter.dpToPx(35);
        final int animTime = 300;
        animHandler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float fraction = (float) elapsed / animTime;
                if (fraction < 1f) {
                    if (mMapHolder != null && mMapHolder.isMapReady()) {
                        mMapHolder.setPadding((int) (targetPadding - val + val * fraction));
                    }
                    animHandler.postDelayed(this, 10);
                } else {
                    if (callback != null) {
                        if (mMapHolder != null && mMapHolder.isMapReady()) {
                            mMapHolder.setPadding(targetPadding);
                        }
                        callback.onPaddingSet();
                    }
                }
            }
        });
    }

    protected void requestRoadLocation(LocationRequestedCallback callback) {
        if (mMapHolder == null) return;
        mLocationRequestedCallback = callback;
        final Location location = mMapHolder.getCurrentRoadLocation();
        if (location != null) {
            triggerLocationCallback(location);
        } else {
            mMapHolder.requestRoadLocation();
        }
    }

    protected void requestGPSLocation(LocationRequestedCallback callback) {
        if (mMapHolder == null) return;
        mLocationRequestedCallback = callback;
        final Location location = mMapHolder.getCurrentLocation();
        if (location != null) {
            triggerLocationCallback(location);
        } else {
            mMapHolder.requestGPSLocation();
        }
    }

    /* Triggers onLocationGranted() callback on the UI Thread if mUnbinder != null */
    private void triggerLocationCallback(final Location location) {
        if (mUnbinder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mUnbinder != null) {
                        getMapActivity().hideLoader();
                        if (mLocationRequestedCallback != null) {
                            mLocationRequestedCallback.onLocationGranted(location);
                            mLocationRequestedCallback = null;
                        }
                    }
                }
            });
        }
    }

    protected void hideAndOnBackPressed(final View bottomView) {
        if (bottomAnimInProgress) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                mMapHolder.setPadding(0);
                if (bottomView.getVisibility() == View.GONE) {
                    getMapActivity().superOnBackPressed();
                } else {
                    bottomView.startAnimation(getAnimTopToBottom(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            bottomAnimInProgress = true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            bottomAnimInProgress = false;
                            getMapActivity().superOnBackPressed();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    }));
                }
            }
        });
    }

    protected void hideAndSetFragment(final View view, final BaseOverlayFragment fragment, final boolean addToBackStack) {
        if (bottomAnimInProgress) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                mMapHolder.setPadding(0);
                if (view.getVisibility() == View.GONE) {
                    if (!addToBackStack) getMapActivity().clearFragmentsBackStaсk();
                    getMapActivity().setOverlayFragment(fragment, addToBackStack);
                } else {
                    view.startAnimation(getAnimTopToBottom(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            bottomAnimInProgress = true;
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            bottomAnimInProgress = false;
                            if (!addToBackStack) getMapActivity().clearFragmentsBackStaсk();
                            getMapActivity().setOverlayFragment(fragment, addToBackStack);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    }));
                }
            }
        });
    }

    protected void show(View view) {
        if (view.getVisibility() == View.VISIBLE) return;
        view.setVisibility(View.VISIBLE);
        view.startAnimation(getAnimBottomToTop(this));
    }

}
