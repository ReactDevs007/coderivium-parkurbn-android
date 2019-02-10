package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cruxlab.parkurbn.LocationManager;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.consts.MapModes;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OverlayCapturedFragment extends BaseOverlayFragment {

    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.ll_bottom_btns)
    LinearLayout llBottomBtns;
    @BindView(R.id.ibtn_my_location)
    ImageButton ibtnMyLocation;

    private Spot mSpot;
    private Segment mSegment;

    private boolean adjustZoom;
    private boolean dialogShown;

    public static OverlayCapturedFragment newInstance() {
        return new OverlayCapturedFragment();
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overlay_captured, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mSpot = SharedPrefsManager.get().getCapturedSpot();
        mSegment = SharedPrefsManager.get().getCapturedSegment();
        if (mSegment != null) {
            SharedPrefsManager.get().saveCapturedSegment(mSegment);
        } else if (mSpot != null) {
            SharedPrefsManager.get().saveCapturedSpot(mSpot);
        }
        requestMap();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (mMapHolder != null) {
            if (mSpot != null) {
                mMapHolder.subscribeSpots(new HashSet<String>());
            } else if (mSegment != null) {
                mMapHolder.subscribeSegments(new HashSet<String>());
            }
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapHolder != null) {
            subscribe();
        }
    }

    /* END LIFECYCLE */
    /* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {
        mMapHolder = mapHolder;
        show(llBottom);
        initCompass();
        mMapHolder.setMinZoom(ZoomLevels.MIN_ROUTE_ZOOM);
        mMapHolder.setMapMode(MapModes.ROUTE);
        adjustZoom = true;
        getRouteToDest();
        mMapHolder.setLocationUpdateFreq(LocationManager.FAST_UPDATE_FREQ);
        subscribe();
    }

    @Override
    public void onLocationUnavailable() {
        super.onLocationUnavailable();
        if (mLocationRequestedCallback.equals(mRouteLocationCallback)) {
            returnToMap();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        getRouteToDest();
    }

    @Override
    public void onSpotTaken(final String id) {
        if (mSpot == null) return;
        if (!mSpot.getId().equals(id)) return;
        if (mUnbinder != null && !dialogShown) {
            dialogShown = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.showCapturedSpotIsTakenDialog(getActivity(), new DialogUtils.ConfirmDialogCallback() {
                        @Override
                        public void okButtonPressed() {
                            if (mUnbinder != null) {
                                arrived();
                            }
                        }

                        @Override
                        public void cancelButtonPressed() {
                            if (mUnbinder != null) {
                                returnToMap();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSegmentChanged(String id, int count) {
        if (mSegment == null) return;
        if (!mSegment.getId().equals(id)) return;
        mSegment.setFreeSpots(count);
        if (mSegment.getFreeSpots() == 0) {
            if (mUnbinder != null && !dialogShown) {
                dialogShown = true;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.showNoFreeSpotsDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                            @Override
                            public void okButtonPressed() {
                                if (mUnbinder != null) {
                                    returnToMap();
                                }
                            }
                        });
                    }
                });
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
                            if (mUnbinder != null) {
                                returnToMap();
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
                                returnToMap();
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
                                returnToMap();
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
                                returnToMap();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onMyLocationCentered(boolean centered) {
        if (mUnbinder != null) {
            ibtnMyLocation.setVisibility(!centered ? View.VISIBLE : View.GONE);
        }
    }


    /* END MAP MANAGER */
    /* ANIM LISTENER */

    @Override
    public void onAnimationEnd(Animation animation) {
        animateGoogleLogo(llBottomBtns.getHeight(), new LogoAnimationCallback() {
            @Override
            public void onPaddingSet() {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.adjustZoomForRoute();
                }
            }
        });
    }

    /* END ANIM LISTENER */
    /* ON CLICK */

    @OnClick(R.id.btn_arrived)
    void arrived() {
        hideAndSetFragment(llBottom, OverlayArrivedFragment.newInstance(), true);
    }

    @OnClick(R.id.btn_cancel)
    void cancel() {
        DialogUtils.showAreYouSureDialog(getActivity(), new DialogUtils.ConfirmDialogCallback() {
            @Override
            public void okButtonPressed() {
                returnToMap();
            }

            @Override
            public void cancelButtonPressed() {

            }
        });
    }

    @OnClick(R.id.ibtn_my_location)
    void centerMyLocation() {
        requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(final Location location) {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.moveToLocation(location.getLatLng());
                }
            }
        });
    }

    /* END ON CLICK */
    /* QUERIES */

    private void getRouteToDest() {
        requestRoadLocation(mRouteLocationCallback);
    }

    /* END QUERIES */
    /* CALLBACKS */

    private PendingResult.Callback<DirectionsResult> mDirectionsResultCallback = new PendingResult.Callback<DirectionsResult>() {
        @Override
        public void onResult(DirectionsResult result) {
            final DirectionsRoute[] routes = result.routes;
            if (routes.length > 0) {
                if (mUnbinder != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mMapHolder != null && mMapHolder.isMapReady()) {
                                LatLng spot = (mSpot != null ? mSpot.getLatLng() : mSegment.getCentralSpot());
                                List<com.google.maps.model.LatLng> route = new ArrayList<>();
                                Location location = mMapHolder.getCurrentRoadLocation();
                                if (location != null) {
                                    route.add(new com.google.maps.model.LatLng(location.getLat(), location.getLng()));
                                }
                                route.addAll(routes[0].overviewPolyline.decodePath());
                                route.add(new com.google.maps.model.LatLng(spot.latitude, spot.longitude));
                                if (mMapHolder != null && mMapHolder.isMapReady()) {
                                    mMapHolder.showRoute(route);
                                    if (location != null) {
                                        mMapHolder.showCar(location.getLatLng());
                                    }
                                    mMapHolder.showDestLoc(spot);
                                    if (adjustZoom) {
                                        mMapHolder.adjustZoomForRoute();
                                        adjustZoom = false;
                                    }
                                }
                            }
                        }
                    });
                }
            } else {
                if (mUnbinder != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mMapHolder != null && mMapHolder.isMapReady()) {
                                mMapHolder.clearMap();
                            }
                            DialogUtils.showNoRouteDialog(getActivity(), mSpot != null, new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {
                                    returnToMap();
                                }
                            });
                        }
                    });
                }
            }
        }

        @Override
        public void onFailure(final Throwable e) {
            e.printStackTrace();
            if (mUnbinder != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    };

    private LocationRequestedCallback mRouteLocationCallback = new LocationRequestedCallback() {
        @Override
        public void onLocationGranted(Location location) {
            mMapHolder.setLocationUpdateFreq(LocationManager.FAST_UPDATE_FREQ);
            Location spot = mSpot != null ? mSpot.getLoc() : mSegment.getLoc();
            DirectionsApi.getDirections(mGeoApiContext, location.toString(), spot.toString())
                    .setCallback(mDirectionsResultCallback);
        }
    };

    /* END CALLBACKS */

    private void returnToMap() {
        SharedPrefsManager.get().clearCapturedSpot();
        SharedPrefsManager.get().clearCapturedSegment();
        hideAndSetFragment(llBottom, OverlayOverviewFragment.newInstance(), false);
    }

    private void subscribe() {
        Set<String> ids = new HashSet<>();
        if (mSpot != null) {
            ids.add(mSpot.getId());
            mMapHolder.subscribeSpots(ids);
        } else if (mSegment != null) {
            ids.add(mSegment.getId());
            mMapHolder.subscribeSegments(ids);
        }
    }

}