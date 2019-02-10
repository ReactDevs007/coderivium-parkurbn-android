package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.MapModes;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OverlaySelectedFragment extends BaseOverlayFragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, BaseInfoFragment.OnRouteExistListener {

    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.btn_capture)
    Button btnCapture;
    @BindColor(R.color.color_orange)
    int colorOrange;
    @BindColor(R.color.color_orange_light)
    int colorOrangeLight;

    private BaseInfoFragment mInfoFragment;
    private Segment mSegment;
    private Spot mSpot;

    private boolean dialogShown;

    public static OverlaySelectedFragment newInstance(Spot spot) {
        OverlaySelectedFragment instance = new OverlaySelectedFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(BundleArguments.SPOT, spot);
        instance.setArguments(arguments);
        return instance;
    }

    public static OverlaySelectedFragment newInstance(Segment segment) {
        OverlaySelectedFragment instance = new OverlaySelectedFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(BundleArguments.SEGMENT, segment);
        instance.setArguments(arguments);
        return instance;
    }

/* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overlay_selected, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mSpot = getArguments().getParcelable(BundleArguments.SPOT);
        mSegment = getArguments().getParcelable(BundleArguments.SEGMENT);
        requestMap();
        show(llBottom);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (mMapHolder != null && mMapHolder.isMapReady()) {
            mMapHolder.setShowOnlyOneMarker(false);
        }
        super.onDestroyView();
    }

/* END LIFECYCLE */
/* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {
        mMapHolder = mapHolder;
        mMapHolder.setMinZoom(ZoomLevels.DEFAULT_MIN);
        initCompass();
        if (mSpot != null) {
            mMapHolder.setZoom(ZoomLevels.SPOTS_MAX, mSpot.getLatLng(), mSpotMarkersCallback);
        } else {
            mMapHolder.setZoom(ZoomLevels.SEGMENTS_MAX, mSegment.getCentralSpot(), mSegmentMarkersCallback);
        }
        if (mSpot != null) {
            mInfoFragment = InfoSpotFragment.newInstance(mSpot, mMapHolder.getCurrentRoadLocation() == null ? null : mMapHolder.getCurrentRoadLocation());
        } else {
            mInfoFragment = InfoSegmentFragment.newInstance(mSegment, mMapHolder.getCurrentRoadLocation() == null ? null : mMapHolder.getCurrentRoadLocation());
        }
        mInfoFragment.setOnRouteExistListener(this);
        getChildFragmentManager().beginTransaction().add(R.id.content_info, mInfoFragment).commit();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        onBackPressed();
    }

    @Override
    public void onSpotSelected(Spot spot) {
        if (mUnbinder != null) {
            if (spot.isFree()) {
                if (mSpot.getId().equals(spot.getId())) {
                    onBackPressed();
                } else {
                    if (mMapHolder != null && mMapHolder.isMapReady()) {
                        mMapHolder.deselectSpot();
                        mSpot = spot;
                        mMapHolder.selectSpot(mSpot);
                        mInfoFragment.setInfo(mSpot);
                        getArguments().putParcelable(BundleArguments.SPOT, mSpot);
                    }
                }
            }
        }
    }

    @Override
    public void onSegmentSelected(Segment segment) {
        if (mUnbinder != null) {
            if (segment.getFreeSpots() > 0) {
                if (mSegment.getId().equals(segment.getId())) {
                    onBackPressed();
                } else {
                    if (mMapHolder != null && mMapHolder.isMapReady()) {
                        mSegment = segment;
                        mMapHolder.selectSegment(mSegment);
                        mInfoFragment.setInfo(mSegment);
                        getArguments().putParcelable(BundleArguments.SEGMENT, mSegment);
                    }
                }
            }
        }
    }

    @Override
    public void onSpotTaken(final String id) {
        if (mSpot == null) return;
        if (mSpot.getId().equals(id)) {
            if (mUnbinder != null && !dialogShown) {
                dialogShown = true;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.showSpotIsTakenDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                            @Override
                            public void okButtonPressed() {
                                if (mUnbinder != null) {
                                    getActivity().onBackPressed();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onSegmentChanged(String id, int count) {
        if (mSegment == null) return;
        if (!mSegment.getId().equals(id)) return;
        mSegment.setFreeSpots(count);
        if (mUnbinder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSegment.getFreeSpots() == 0 && !dialogShown) {
                        dialogShown = true;
                        DialogUtils.showNoFreeSpotsDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                            @Override
                            public void okButtonPressed() {
                                if (mUnbinder != null) {
                                    getActivity().onBackPressed();
                                }
                            }
                        });
                    } else {
                        mInfoFragment.setInfo(mSegment);
                        getArguments().putParcelable(BundleArguments.SEGMENT, mSegment);
                    }
                }
            });
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
                            if (mUnbinder != null) {
                                getActivity().onBackPressed();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSegmentNoLongerSatisfyFilter(String id) {
        if (mSegment == null) return;
        if (!mSegment.getId().equals(id)) return;
        if (mUnbinder != null && !dialogShown) {
            dialogShown = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showSegmentNoLongerSatisfyFilterDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                        @Override
                        public void okButtonPressed() {
                            if (mUnbinder != null) {
                                getActivity().onBackPressed();
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
                            if (mUnbinder != null) {
                                getActivity().onBackPressed();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSegmentUnavailable(String id) {
        if (mSegment == null) return;
        if (!mSegment.getId().equals(id)) return;
        if (mUnbinder != null && !dialogShown) {
            dialogShown = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showSegmentIsUnavailableDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                        @Override
                        public void okButtonPressed() {
                            if (mUnbinder != null) {
                                getActivity().onBackPressed();
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
    /* ON CLICK */

    @OnClick(R.id.btn_capture)
    void capture() {
        requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(final Location location) {
                if (mInfoFragment.isRouteExists() == -1) {
                    getMapActivity().showLoader();
                    mInfoFragment.setCurLocation(location);
                } else if (mInfoFragment.isRouteExists() == 1) {
                    setCapturedFragment();
                } else {
                    showNoRouteDialog();
                }
            }
        });
    }

    /* END ON CLICK */
    /* CALLBACKS */

    private GoogleMap.CancelableCallback mSpotMarkersCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
            if (mMapHolder != null && mMapHolder.isMapReady()) {
                mMapHolder.setMinZoom(ZoomLevels.SEGMENTS_MAX);
                mMapHolder.setMapMode(MapModes.SPOT_MARKERS);
                mMapHolder.selectSpot(mSpot);
            }
        }

        @Override
        public void onCancel() {
            onFinish();
        }
    };

    private GoogleMap.CancelableCallback mSegmentMarkersCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
            if (mMapHolder != null && mMapHolder.isMapReady()) {
                mMapHolder.setMaxZoom(ZoomLevels.SEGMENTS_MAX);
                if (mMapHolder.getMapMode() == MapModes.SEGMENTS) {
                    mMapHolder.setShowOnlyOneMarker(true);
                }
                mMapHolder.setMapMode(MapModes.SEGMENT_MARKERS);
                mMapHolder.selectSegment(mSegment);
            }
        }

        @Override
        public void onCancel() {
            onFinish();
        }
    };

    @Override
    public void onAnimationEnd(Animation animation) {
        animateGoogleLogo(llBottom.getHeight());
    }

    @Override
    public void onRouteExists(boolean exists) {
        if (getMapActivity().isLoaderShown()) {
            getMapActivity().hideLoader();
            if (exists) {
                setCapturedFragment();
            } else {
                showNoRouteDialog();
            }
        }
        if (exists) {
            btnCapture.setBackgroundResource(R.drawable.bg_app_button);
            btnCapture.setTextColor(colorOrange);
        } else {
            btnCapture.setBackgroundResource(R.drawable.bg_app_button_disabled);
            btnCapture.setTextColor(colorOrangeLight);
        }
    }

    /* END CALLBACKS */

    private void setCapturedFragment() {
        if (mSpot != null) {
            SharedPrefsManager.get().saveCapturedSpot(mSpot);
        } else {
            SharedPrefsManager.get().saveCapturedSegment(mSegment);
        }
        hideAndSetFragment(llBottom, OverlayCapturedFragment.newInstance(), false);
    }

    private void showNoRouteDialog() {
        DialogUtils.showNoRouteDialog(getActivity(), mSpot != null, null);
    }

}