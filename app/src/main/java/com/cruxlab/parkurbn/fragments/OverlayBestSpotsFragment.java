package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cruxlab.parkurbn.LocationManager;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.MapModes;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.custom.SwipingIndicatorsLayout;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.request.SpotsRequest;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
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

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class OverlayBestSpotsFragment extends BaseOverlayFragment implements ViewPager.OnPageChangeListener, BaseInfoFragment.OnRouteExistListener {

    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.vp_spots)
    ViewPager vpSpots;
    @BindView(R.id.sil)
    SwipingIndicatorsLayout indicatorsLayout;
    @BindView(R.id.btn_navigate)
    Button btnNavigate;
    @BindColor(R.color.color_orange)
    int colorOrange;
    @BindColor(R.color.color_orange_light)
    int colorOrangeLight;

    private static final int SPOTS_CNT = 10;
    private boolean areSpotsLoaded;

    private SpotFragmentPagerAdapter mSpotAdapter;
    private int prevPosition = 500;
    private boolean shouldAdjustZoom;
    private Call<List<Spot>> spotsRequest;
    private boolean dialogShown;

    public static OverlayBestSpotsFragment newInstance() {
        return new OverlayBestSpotsFragment();
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overlay_best_spots, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mSpotAdapter = new SpotFragmentPagerAdapter(getChildFragmentManager());
        vpSpots.addOnPageChangeListener(this);
        vpSpots.setOffscreenPageLimit(1);
        vpSpots.setAdapter(mSpotAdapter);
        requestMap();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!areSpotsLoaded) {
            getBestDistanceSpots();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        areSpotsLoaded = false;
    }

    @Override
    public void onDestroyView() {
        if (mMapHolder != null) {
            mMapHolder.subscribeSpots(new HashSet<String>());
        }
        super.onDestroyView();
    }

    /* END LIFECYCLE */
    /* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {
        mMapHolder = mapHolder;
        mMapHolder.setMinZoom(ZoomLevels.MIN_ROUTE_ZOOM);
        initCompass();
        mMapHolder.setMapMode(MapModes.ROUTE);
        mMapHolder.setLocationUpdateFreq(LocationManager.FAST_UPDATE_FREQ);
        getBestDistanceSpots();
    }

    @Override
    public void onLocationUnavailable() {
        super.onLocationUnavailable();
        areSpotsLoaded = true;
        getActivity().onBackPressed();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (areSpotsLoaded) {
            getRouteToSpot();
        }
    }

    @Override
    public void onSpotTaken(final String id) {
        if (mUnbinder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSpotAdapter.getSpotsCount() == 1) {
                        if (!dialogShown) {
                            dialogShown = true;
                            DialogUtils.showNoFreeSpotsDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {
                                    if (mUnbinder != null) {
                                        getActivity().onBackPressed();
                                    }
                                }
                            });
                        }
                    } else {
                        mSpotAdapter.removeSpotById(id);
                        if (mSpotAdapter.getCurrentSpot().getId().equals(id)) {
                            DialogUtils.showSpotIsTakenDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onSpotNoLongerSatisfyFilter(final String id) {
        if (mUnbinder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSpotAdapter.getSpotsCount() == 1) {
                        if (!dialogShown) {
                            dialogShown = true;
                            DialogUtils.showNoFreeSpotsDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {
                                    if (mUnbinder != null) {
                                        getActivity().onBackPressed();
                                    }
                                }
                            });
                        }
                    } else {
                        mSpotAdapter.removeSpotById(id);
                        if (mSpotAdapter.getCurrentSpot().getId().equals(id)) {
                            DialogUtils.showSpotNoLongerSatisfyFilterDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {

                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onSpotUnavailable(final String id) {
        if (mUnbinder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSpotAdapter.getSpotsCount() == 1) {
                        if (!dialogShown) {
                            dialogShown = true;
                            DialogUtils.showNoFreeSpotsDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {
                                    if (mUnbinder != null) {
                                        getActivity().onBackPressed();
                                    }
                                }
                            });
                        }
                    } else {
                        mSpotAdapter.removeSpotById(id);
                        if (mSpotAdapter.getCurrentSpot().getId().equals(id)) {
                            DialogUtils.showSpotIsUnavailableDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                                @Override
                                public void okButtonPressed() {

                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        onBackPressed();
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
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.adjustZoomForRoute();
                }
                shouldAdjustZoom = true;
            }
        });
    }

    /* END ANIM LISTENER */
    /* ON CLICK */

    @OnClick(R.id.btn_navigate)
    void navigate() {
        SharedPrefsManager.get().saveCapturedSpot(mSpotAdapter.getCurrentSpot());
        hideAndSetFragment(llBottom, OverlayCapturedFragment.newInstance(), false);
        shouldAdjustZoom = false;
    }

    /* END ON CLICK */
    /* QUERIES */

    private void getBestDistanceSpots() {
        requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(final Location location) {
                if (spotsRequest == null) {
                    getMapActivity().showLoader();
                    spotsRequest = mParkUrbnApi.getBestDistanceSpots(new SpotsRequest(location, SPOTS_CNT, mMapHolder.getMinTime()));
                    spotsRequest.enqueue(mBestSpotsCallback);
                }
            }
        });
    }

    private void getRouteToSpot() {
        requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(Location location) {
                if (mSpotAdapter.getCount() > 0) {
                    DirectionsApi.getDirections(mGeoApiContext, location.toString(), mSpotAdapter.getCurrentSpot().getLoc().toString()).setCallback(mDirectionsResultCallback);
                }
            }
        });
    }

    /* END QUERIES */
    /* CALLBACKS */

    private PendingResult.Callback<DirectionsResult> mDirectionsResultCallback = new PendingResult.Callback<DirectionsResult>() {
        @Override
        public void onResult(final DirectionsResult result) {
            final DirectionsRoute[] routes = result.routes;
            if (routes.length > 0) {
                if (mUnbinder != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mMapHolder != null && mMapHolder.isMapReady()) {
                                Spot spot = mSpotAdapter.getCurrentSpot();
                                List<com.google.maps.model.LatLng> route = new ArrayList<>();
                                Location location = mMapHolder.getCurrentRoadLocation();
                                if (location != null) {
                                    route.add(new com.google.maps.model.LatLng(location.getLat(), location.getLng()));
                                }
                                route.addAll(routes[0].overviewPolyline.decodePath());
                                route.add(new com.google.maps.model.LatLng(spot.getLat(), spot.getLon()));
                                if (mMapHolder != null && mMapHolder.isMapReady()) {
                                    mMapHolder.showRoute(route);
                                    if (location != null) {
                                        mMapHolder.showCar(location.getLatLng());
                                    }
                                    mMapHolder.showDestLoc(spot.getLatLng());
                                    if (shouldAdjustZoom) {
                                        mMapHolder.adjustZoomForRoute();
                                        shouldAdjustZoom = false;
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
                            Toast.makeText(getActivity(), getString(R.string.no_route_to_spot), Toast.LENGTH_SHORT).show();
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
                        if (mUnbinder != null) {
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
                            getActivity().onBackPressed();
                        }
                    }
                });
            }
        }
    };

    private ResponseCallback<List<Spot>> mBestSpotsCallback = new ResponseCallback<List<Spot>>() {
        @Override
        public void handleResponse(Response<List<Spot>> response) {
            spotsRequest = null;
            if (mUnbinder != null) {
                areSpotsLoaded = true;
                getMapActivity().hideLoader();
                mSpotAdapter.setSpots((ArrayList<Spot>) response.body());
                vpSpots.setCurrentItem(mSpotAdapter.getCount() / 2, false);
                Set<String> ids = new HashSet<>();
                for (Spot spot : mSpotAdapter.mSpots) ids.add(spot.getId());
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.subscribeSpots(ids);
                }
                show(llBottom);
                getRouteToSpot();
            }
        }

        @Override
        protected void showErrorMessage(ErrorResponse response) {
            spotsRequest = null;
            if (mUnbinder != null) {
                areSpotsLoaded = false;
                getMapActivity().hideLoader();
                DialogUtils.showNoSpotsAroundDialog(getActivity(), new DialogUtils.InfoDialogCallback() {
                    @Override
                    public void okButtonPressed() {
                        getActivity().onBackPressed();
                    }
                });
            }
        }

        @Override
        public void onFailure(Call<List<Spot>> call, Throwable t) {
            super.onFailure(call, t);
            showErrorMessage(null);
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (prevPosition > position) indicatorsLayout.animateRight();
        else indicatorsLayout.animateLeft();
        if (prevPosition != position) {
            prevPosition = position;
            shouldAdjustZoom = true;
            getRouteToSpot();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRouteExists(boolean exists) {
        if (exists) {
            btnNavigate.setBackgroundResource(R.drawable.bg_app_button);
            btnNavigate.setTextColor(colorOrange);
        } else {
            btnNavigate.setBackgroundResource(R.drawable.bg_app_button_disabled);
            btnNavigate.setTextColor(colorOrangeLight);
        }
    }

    /* END CALLBACKS */
    /* FRAGMENT ADAPTER */

    private class SpotFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private int LOOPS_COUNT = 1000;
        private ArrayList<Spot> mSpots;

        SpotFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            mSpots = new ArrayList<>();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mSpots.size() * LOOPS_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return InfoSpotFragment.newInstance(mSpots.get(position % mSpots.size()),
                    mMapHolder.getCurrentRoadLocation() == null ? null : mMapHolder.getCurrentRoadLocation());
        }

        void setSpots(ArrayList<Spot> spots) {
            mSpots = spots;
            notifyDataSetChanged();
            vpSpots.setCurrentItem(0);
        }

        void removeSpotById(String id) {
            Spot spotToRemove = null;
            for (Spot spot : mSpots) {
                if (spot.getId().equals(id)) {
                    spotToRemove = spot;
                    break;
                }
            }
            if (spotToRemove != null) {
                mSpots.remove(spotToRemove);
                notifyDataSetChanged();
            }
        }

        private Spot getCurrentSpot() {
            return mSpots.get(vpSpots.getCurrentItem() % mSpots.size());
        }

        public int getSpotsCount() {
            return mSpots.size();
        }

    }

    /* END FRAGMENT ADAPTER */

}
