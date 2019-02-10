package com.cruxlab.parkurbn.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.MapActivity;
import com.cruxlab.parkurbn.activities.OrderActivity;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.MapModes;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.request.SpotsRequest;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.tools.BitmapUtils;
import com.cruxlab.parkurbn.tools.Converter;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class OverlayArrivedFragment extends BaseOverlayFragment {

    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.iv_confirm_marker)
    ImageView ivConfirmMarker;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @BindColor(R.color.color_orange)
    int colorOrange;
    @BindColor(R.color.color_orange_light)
    int colorOrangeLight;

    public static int SCREENSHOT_HEIGHT_DP = 130;

    private Spot mSpot;
    private long clientArrivedMillis;
    private AtomicBoolean mapAnimInProgress;

    private boolean isSelected;
    private boolean dialogShown;

    public static OverlayArrivedFragment newInstance() {
        return new OverlayArrivedFragment();
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overlay_arrived, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mapAnimInProgress = new AtomicBoolean(false);
        requestMap();
        return rootView;
    }

    /* END LIFECYCLE */
    /* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {
        mMapHolder = mapHolder;
        mMapHolder.setMapMode(MapModes.SPOT_MARKERS);
        initCompass();
        /*requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(Location location) {
                show(llBottom);
            }
        });*/
        show(llBottom);
    }

    @Override
    public void onCameraStoppedMoving(LatLng center) {
        if (mMapHolder != null && mMapHolder.isMapReady()) {
            if (mapAnimInProgress.get()) {
                mapAnimInProgress.set(false);
            } else {
                getClosestSpot(mMoveCallback, mMapHolder.getCenter());
            }
        }
    }

    @Override
    public void onSpotNoLongerSatisfyFilter(String id) {
        if (mSpot == null) return;
        if (!mSpot.getId().equals(id)) return;
        if (mUnbinder != null && !dialogShown) {
            dialogShown = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showSpotNoLongerSatisfyFilterDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                        @Override
                        public void okButtonPressed() {
                            dialogShown = false;
                            if (mUnbinder != null && mMapHolder != null && mMapHolder.isMapReady()) {
                                getClosestSpot(mMoveCallback, mMapHolder.getCenter());
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSpotUnavailable(String id) {
        if (mSpot == null) return;
        if (!mSpot.getId().equals(id)) return;
        if (mUnbinder != null && !dialogShown) {
            dialogShown = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showSpotIsUnavailableDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                        @Override
                        public void okButtonPressed() {
                            dialogShown = false;
                            if (mUnbinder != null && mMapHolder != null && mMapHolder.isMapReady()) {
                                getClosestSpot(mMoveCallback, mMapHolder.getCenter());
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        hideAndOnBackPressed(llBottom);
    }

    /* END MAP MANAGER */
    /* ANIM LISTENER */

    @Override
    public void onAnimationEnd(Animation animation) {
        animateGoogleLogo(llBottom.getHeight(), new LogoAnimationCallback() {
            @Override
            public void onPaddingSet() {
                if (mUnbinder != null) {
                    getMapActivity().showLoader();
                    /*if (mMapHolder != null && mMapHolder.isMapReady() && mMapHolder.getCurrentRoadLocation() != null) {
                        getClosestSpot(mInitCallback, mMapHolder.getCurrentRoadLocation().getLatLng());
                    } else {
                        getMapActivity().hideLoader();
                    }*/
                    ivConfirmMarker.setVisibility(View.VISIBLE);
                    if (mMapHolder != null && mMapHolder.isMapReady()) {
                        getClosestSpot(mInitCallback, mMapHolder.getCenter());
                    } else {
                        getMapActivity().hideLoader();
                    }
                }
            }
        });
    }

    /* END ANIM LISTENER */
    /* ON CLICK */

    @OnClick(R.id.btn_confirm)
    void confirm() {
        getMapActivity().showLoader();
        getClosestSpot(mConfirmCallback, mMapHolder.getCenter());
        isSelected = true;
    }

    /* END ON CLICK */
    /* QUERIES */

    private void getClosestSpot(ResponseCallback<List<Spot>> callback, LatLng latLng) {
        if (isSelected) return;
        clientArrivedMillis = System.currentTimeMillis();
        mParkUrbnApi.arrived(new SpotsRequest(new Location(latLng), 1, mMapHolder.getMinTime())).enqueue(callback);
    }

    /* END QUERIES */
    /* CALLBACKS */

    private ResponseCallback<List<Spot>> mMoveCallback = new ResponseCallback<List<Spot>>() {
        @Override
        public void onFailure(Call<List<Spot>> call, Throwable t) {
            handleErrorResponse();
        }

        @Override
        protected void showErrorMessage(ErrorResponse response) {
            handleErrorResponse();
        }

        @Override
        public void handleResponse(Response<List<Spot>> response) {
            if (mUnbinder != null) {
                ArrayList<Spot> spots = (ArrayList<Spot>) response.body();
                if (spots.size() > 0) {
                    handleSuccessfulResponse(spots.get(0));
                    zoomTo(spots.get(0).getLatLng());
                } else {
                    handleErrorResponse();
                }
            }
        }
    };

    private ResponseCallback<List<Spot>> mConfirmCallback = new ResponseCallback<List<Spot>>() {
        @Override
        public void onFailure(Call<List<Spot>> call, Throwable t) {
            isSelected = false;
            handleErrorResponse();
        }

        @Override
        protected void showErrorMessage(ErrorResponse response) {
            isSelected = false;
            handleErrorResponse();
        }

        @Override
        public void handleResponse(Response<List<Spot>> response) {
            if (mUnbinder != null) {
                final ArrayList<Spot> spots = (ArrayList<Spot>) response.body();
                if (spots.size() > 0) {
                    handleSuccessfulResponse(spots.get(0));
                    zoomTo(spots.get(0).getLatLng(), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            if (mUnbinder != null) {
                                selectSpot(spots.get(0));
                            }
                        }

                        @Override
                        public void onCancel() {
                            mapAnimInProgress.set(false);
                            onFinish();
                        }
                    });
                } else {
                    handleErrorResponse();
                }
            }
        }
    };

    private ResponseCallback<List<Spot>> mInitCallback = new ResponseCallback<List<Spot>>() {
        @Override
        public void onFailure(Call<List<Spot>> call, Throwable t) {
            handleErrorResponse();
            if (mUnbinder != null) {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    /*Location location = mMapHolder.getCurrentRoadLocation();
                    if (location != null) {
                        zoomTo(location.getLatLng());
                    }*/
                    zoomTo(mMapHolder.getCenter());
                }
            }
        }

        @Override
        protected void showErrorMessage(ErrorResponse response) {
            handleErrorResponse();
            if (mUnbinder != null) {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    /*Location location = mMapHolder.getCurrentRoadLocation();
                    if (location != null) {
                        zoomTo(location.getLatLng());
                    }*/
                    zoomTo(mMapHolder.getCenter());
                }
            }
        }

        @Override
        public void handleResponse(Response<List<Spot>> response) {
            if (mUnbinder != null) {
                final ArrayList<Spot> spots = (ArrayList<Spot>) response.body();
                if (spots.size() > 0) {
                    handleSuccessfulResponse(spots.get(0));
                    zoomTo(spots.get(0).getLatLng());
                } else {
                    handleErrorResponse();
                    if (mMapHolder != null && mMapHolder.isMapReady()) {
                        /*Location location = mMapHolder.getCurrentRoadLocation();
                        if (location != null) {
                            zoomTo(location.getLatLng());
                        }*/
                        zoomTo(mMapHolder.getCenter());
                    }
                }
            }
        }
    };

    /* END CALLBACKS */

    private void showBottomViews() {
        if (llBottom.getVisibility() == View.VISIBLE) return;
        llBottom.setVisibility(View.VISIBLE);
        llBottom.startAnimation(getAnimBottomToTop(this));
    }

    private void zoomTo(LatLng latLng) {
        zoomTo(latLng, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if (mUnbinder != null && mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.setMinZoom(ZoomLevels.DEFAULT);
                    /*ivConfirmMarker.setVisibility(View.VISIBLE);*/
                }
            }

            @Override
            public void onCancel() {
                mapAnimInProgress.set(false);
                onFinish();
            }
        });
    }

    private void zoomTo(LatLng latLng, GoogleMap.CancelableCallback cancelableCallback) {
        if (mMapHolder != null && mMapHolder.isMapReady()) {
            mapAnimInProgress.set(true);
            mMapHolder.setZoom(ZoomLevels.DEFAULT_MAX, latLng, cancelableCallback);
        }
    }

    private void selectSpot(final Spot spot) {
        if (mMapHolder != null && mMapHolder.isMapReady()) {
            mMapHolder.takeMapScreenshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    Bitmap croppedBitmap = BitmapUtils.centerCrop(bitmap, llBottom.getHeight(), Converter.dpToPx(SCREENSHOT_HEIGHT_DP));
                    BitmapUtils.save(getActivity(), croppedBitmap, MapActivity.SCREENSHOT_NAME);
                    Intent parkingIntent = new Intent(getActivity(), OrderActivity.class);
                    spot.setClientArrivedMillis(clientArrivedMillis);
                    parkingIntent.putExtra(BundleArguments.SPOT, spot);
                    startActivity(parkingIntent);
                    isSelected = false;
                }
            });
        }
    }

    private void handleErrorResponse() {
        if (mUnbinder != null) {
            getMapActivity().hideLoader();
            tvAddress.setText(getString(R.string.move_pin));
            btnConfirm.setEnabled(false);
            btnConfirm.setBackgroundResource(R.drawable.bg_app_button_disabled);
            btnConfirm.setTextColor(colorOrangeLight);
        }
    }

    private void handleSuccessfulResponse(Spot spot) {
        if (mUnbinder != null) {
            getMapActivity().hideLoader();
            mSpot = spot;
            btnConfirm.setEnabled(true);
            btnConfirm.setBackgroundResource(R.drawable.bg_app_button);
            btnConfirm.setTextColor(colorOrange);
            tvAddress.setText(spot.getAddress());
        }
    }

}
