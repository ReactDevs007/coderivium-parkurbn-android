package com.cruxlab.parkurbn.fragments;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchResult;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.MapModes;
import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.PlaceSuggestion;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.tools.Converter;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class OverlayOverviewFragment extends BaseOverlayFragment {

    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.btn_free_spot_cnt)
    Button btnFreeSpotCnt;
    @BindView(R.id.ibtn_my_location)
    ImageButton ibtnMyLocation;
    @BindView(R.id.search_bar)
    LinearLayout searchBar;
    @BindView(R.id.sb_time)
    SeekBar sbTime;
    @BindView(R.id.tv_cur_min_time)
    TextView tvCurMinTime;
    @BindView(R.id.ll_filter)
    LinearLayout llFilter;
    @BindView(R.id.ibtn_filter)
    ImageButton ibtnFilter;
    @BindView(R.id.floating_search_view)
    FloatingSearchView searchView;
    @BindView(R.id.btn_pay)
    Button btnPay;
    @BindView(R.id.ibtn_quick_parking)
    ImageButton ibtnQuickParking;
    @BindView(R.id.ibtn_compass)
    ImageButton ibtnCompass;
    @BindView(R.id.rl_overlay_btns)
    RelativeLayout rlOverlayBtns;

    @BindColor(R.color.color_orange)
    int colorOrange;

    private boolean isSearchShown;
    private boolean isFilterShown;
    private boolean shouldClearSearch;

    private static int ANIM_TIME = 700;
    private static int FILTERS_LAYOUT_HEIGHT_DP = 120;
    private static int BOTTOM_BAR_HEIGHT_DP = 48;
    private static int DELAYED_MESSAGE = 0;
    private static int DELAY = 500;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            searchView.showProgress();
            findLocations(msg.obj.toString());
        }
    };

    public static OverlayOverviewFragment newInstance() {
        OverlayOverviewFragment instance = new OverlayOverviewFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(BundleArguments.SHOW_MARKERS, false);
        instance.setArguments(arguments);
        return instance;
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_overlay_overview, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        requestMap();
        initSearchView();
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        shouldClearSearch = false;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchView.isSearchBarFocused()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchView, 0);
        } else if (shouldClearSearch) {
            clearSearch();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.SPEECH_RECOGNIZER:
                if (resultCode == RESULT_OK) {
                    List<String> matches = data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    processSpeechRecognizerResults(matches);
                }
                break;
        }
    }

    /* END LIFECYCLE */
    /* MAP MANAGER */

    @Override
    public void onMapReady(MapHolder mapHolder) {
        mMapHolder = mapHolder;
        initCompass();
        mapHolder.setMinZoom(ZoomLevels.DEFAULT_MIN);
        mMapHolder.setPadding(Converter.dpToPx(BOTTOM_BAR_HEIGHT_DP));
        if (getArguments().getBoolean(BundleArguments.SHOW_MARKERS, false)) {
            btnFreeSpotCnt.setTextColor(colorOrange);
        }
        mMapHolder.updateFreeSpotCnt();
        updateMode();
        sbTime.setOnSeekBarChangeListener(mTimeListener);
        if (mMapHolder.getCurrentRoadLocation() != null) {
            mMapHolder.showCar(mMapHolder.getCurrentRoadLocation().getLatLng());
        } else if (mMapHolder.getCurrentLocation() != null) {
            mMapHolder.showCar(mMapHolder.getCurrentLocation().getLatLng());
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mUnbinder != null && isFilterShown) {
            hideFilter();
        }
    }

    @Override
    public void onZoomChanged(float zoom) {
        if (mUnbinder != null) {
            updateMode();
        }
    }

    @Override
    public void onFreeSpotCntChanged(int freeCnt) {
        if (mUnbinder != null) {
            btnFreeSpotCnt.setText(String.valueOf(freeCnt));
        }
    }

    @Override
    public void onMyLocationCentered(boolean centered) {
        if (mUnbinder != null) {
            ibtnMyLocation.setVisibility(!centered && !isSearchShown ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onSpotSelected(Spot spot) {
        if (mUnbinder != null) {
            if (spot.isFree()) {
                getMapActivity().setOverlayFragment(OverlaySelectedFragment.newInstance(spot), true);
            }
        }
    }

    @Override
    public void onSegmentSelected(Segment segment) {
        if (mUnbinder != null) {
            if (segment.getFreeSpots() > 0) {
                getMapActivity().setOverlayFragment(OverlaySelectedFragment.newInstance(segment), true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isFilterShown) {
            hideFilter();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (mUnbinder != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMapHolder != null && mMapHolder.isMapReady()) {
                        mMapHolder.showCar(location.getLatLng());
                    }
                }
            });
        }
    }

    /* END MAP MANAGER */
    /* ON CLICK */

    @OnClick(R.id.btn_pay)
    void pay() {
        requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(Location location) {
                getMapActivity().setOverlayFragment(OverlayArrivedFragment.newInstance(), true);
            }
        });
    }

    @OnClick(R.id.ibtn_quick_parking)
    void quickParking() {
        requestRoadLocation(new LocationRequestedCallback() {
            @Override
            public void onLocationGranted(Location location) {
                getMapActivity().setOverlayFragment(OverlayBestSpotsFragment.newInstance(), true);
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

    @OnClick(R.id.ll_filter)
    void preventOnMapClick() {

    }

    @OnClick(R.id.ibtn_filter)
    void filterClick() {
        if (llFilter.getVisibility() == View.VISIBLE) {
            hideFilter();
        } else {
            showFilter();
        }
    }

    @OnClick(R.id.btn_free_spot_cnt)
    void freeSpotCntClick() {
        if (getArguments().getBoolean(BundleArguments.SHOW_MARKERS, false)) {
            getArguments().putBoolean(BundleArguments.SHOW_MARKERS, false);
            btnFreeSpotCnt.setTextColor(Color.BLACK);
            updateMode();
        } else {
            getArguments().putBoolean(BundleArguments.SHOW_MARKERS, true);
            btnFreeSpotCnt.setTextColor(colorOrange);
            if (mMapHolder.getZoom() > ZoomLevels.SEGMENTS_MAX) {
                mMapHolder.setZoom(ZoomLevels.SEGMENTS_MAX, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (mMapHolder != null && mMapHolder.isMapReady()) {
                            updateMode();
                        }
                    }

                    @Override
                    public void onCancel() {
                        onFinish();
                    }
                });
            } else {
                mMapHolder.setMapMode(MapModes.SEGMENT_MARKERS);
            }
        }
    }

    /* END ON CLICK */
    /* SEARCH */

    private void processSpeechRecognizerResults(List<String> results) {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setCancelable(false);
        pd.show();

        List<Address> addresses = new ArrayList<>();

        Geocoder gc = new Geocoder(getContext());
        if (!results.isEmpty()) {
            try {
                addresses.addAll(gc.getFromLocationName(results.get(0), 5));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!addresses.isEmpty()) {
            if (addresses.size() == 1) {
                Address address = addresses.iterator().next();
                updateSearchUI(new LatLng(address.getLatitude(), address.getLongitude()),
                        address.getLocality());
            } else {
                showChooseAddressDialog(addresses);
            }
        } else {
            Toast.makeText(getContext(), "Can't find any place. Try again.", Toast.LENGTH_SHORT).show();
        }

        pd.dismiss();
    }

    private void showChooseAddressDialog(final List<Address> addresses) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose your address:");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        for (Address address : addresses) {
            String a = address.getLocality() != null ? address.getLocality() : "";
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                a = a.concat(address.getAddressLine(i));
                if (i != address.getMaxAddressLineIndex()) {
                    a = a.concat(", ");
                }
            }
            arrayAdapter.add(a);
        }

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateSearchUI(new LatLng(addresses.get(i).getLatitude(), addresses.get(i).getLongitude()));
                searchView.setSearchText(addresses.get(0).getLocality());
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void updateSearchUI(LatLng latLng, String name) {
        searchView.setSearchText(name);
        updateSearchUI(latLng);
    }

    private void updateSearchUI(LatLng latLng) {
        mMapHolder.addSearchMarker(latLng);
        mMapHolder.setZoom(ZoomLevels.DEFAULT, latLng);
        searchView.clearSearchFocus();
    }

    private void initSearchView() {
        shouldClearSearch = true;
        searchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                openDrawer();
                //return the hamburger icon
                searchView.closeMenu(true);
            }

            @Override
            public void onMenuClosed() {
            }
        });

        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                isSearchShown = true;
                setViewsVisibility(View.GONE);
            }

            @Override
            public void onFocusCleared() {
                isSearchShown = false;
                setViewsVisibility(View.VISIBLE);
            }
        });

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                } else {
                    mHandler.removeMessages(DELAYED_MESSAGE);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(DELAYED_MESSAGE, newQuery), DELAY);
                }
            }
        });

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchResult searchSuggestion) {
                Places.GeoDataApi.getPlaceById(mMapHolder.getGoogleApiClient(), ((PlaceSuggestion) searchSuggestion).getPlaceId())
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                    updateSearchUI(places.get(0).getLatLng());
                                }
                                places.release();
                            }
                        });
                searchView.inflateOverflowMenu(R.menu.menu_search_view_with_close);
            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });

        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.action_voice_rec) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                    startActivityForResult(intent, RequestCodes.SPEECH_RECOGNIZER);
                } else {
                    mMapHolder.removeSearchMarker();
                    clearSearch();
                }
            }
        });
    }

    private void clearSearch() {
        searchView.clearQuery();
        searchView.inflateOverflowMenu(R.menu.menu_search_view);
    }

    private void setViewsVisibility(int visibility) {
        llBottom.setVisibility(visibility);
        ibtnFilter.setVisibility(visibility);
        btnPay.setVisibility(visibility);
        btnFreeSpotCnt.setVisibility(visibility);
        ibtnMyLocation.setVisibility(visibility);
        ibtnQuickParking.setVisibility(visibility);
        ibtnCompass.setVisibility(visibility == View.GONE ? View.INVISIBLE : visibility);
        if (visibility == View.GONE) llFilter.setVisibility(visibility);
        else if (isFilterShown) {
            llFilter.setVisibility(visibility);
        }
        if (mMapHolder != null && mMapHolder.isMapReady()) {
            mMapHolder.setPadding(visibility == View.GONE ? 0 : Converter.dpToPx(BOTTOM_BAR_HEIGHT_DP));
        }
    }

    private void findLocations(String query) {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .build();

        double radiusDegrees = 0.10;
        Location location = mMapHolder.getCurrentRoadLocation();
        LatLngBounds bounds;
        if (location != null) {
            LatLng northEast = new LatLng(location.getLat() + radiusDegrees, location.getLng() + radiusDegrees);
            LatLng southWest = new LatLng(location.getLat() - radiusDegrees, location.getLng() - radiusDegrees);
            bounds = LatLngBounds.builder().include(northEast).include(southWest).build();
        } else {
            bounds = mMapHolder.getBounds();
        }

        Places.GeoDataApi.getAutocompletePredictions(mMapHolder.getGoogleApiClient(), query, bounds, typeFilter)
                .setResultCallback(
                        new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult(@NonNull AutocompletePredictionBuffer buffer) {
                                if (buffer.getStatus().isSuccess()) {
                                    List<PlaceSuggestion> resultList = new ArrayList<>();
                                    for (AutocompletePrediction prediction : buffer) {
                                        resultList.add(0, new PlaceSuggestion(prediction.getPlaceId(),
                                                prediction.getPrimaryText(null).toString(), prediction.getSecondaryText(null).toString()));
                                    }

                                    searchView.hideProgress();
                                    if (searchView.isSearchBarFocused()) {
                                        searchView.swapSuggestions(resultList);
                                    }
                                }

                                //Prevent memory leak by releasing buffer
                                buffer.release();
                            }
                        }, 60, TimeUnit.SECONDS);
    }

    /* END SEARCH */

    private void updateMode() {
        int prevMode = mMapHolder.getMapMode();
        int newMode;
        float zoom = mMapHolder.getZoom();
        if (zoom <= ZoomLevels.SEGMENTS_MAX) {
            newMode = getArguments().getBoolean(BundleArguments.SHOW_MARKERS, false) ? MapModes.SEGMENT_MARKERS : MapModes.SEGMENTS;
        } else {
            newMode = MapModes.SPOT_MARKERS;
        }
        if (prevMode != newMode) {
            mMapHolder.setMapMode(newMode);
        }
    }

    private void showFilter() {
        isFilterShown = true;
        int minTime = 10 + (int) (4.70 * sbTime.getProgress());;
        tvCurMinTime.setText(String.valueOf(minTime / 60) + "h " + String.valueOf(minTime % 60) + "m");
        ibtnFilter.setImageResource(R.drawable.ic_filter_selected);
        llFilter.setVisibility(View.VISIBLE);
        llFilter.startAnimation(getAnimBottomToTop());
        final int translationPx = Converter.dpToPx(FILTERS_LAYOUT_HEIGHT_DP);
        rlOverlayBtns.animate().translationY(-translationPx).setDuration(ANIM_TIME).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.setPadding(Math.round(Converter.dpToPx(BOTTOM_BAR_HEIGHT_DP) + animation.getAnimatedFraction() * translationPx));
                }
            }
        }).start();
    }

    private void hideFilter() {
        isFilterShown = false;
        ibtnFilter.setImageResource(R.drawable.ic_filter);
        llFilter.startAnimation(getAnimTopToBottom());
        llFilter.setVisibility(View.GONE);
        rlOverlayBtns.animate().translationY(0).setDuration(ANIM_TIME).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mMapHolder != null && mMapHolder.isMapReady()) {
                    mMapHolder.setPadding(Math.round(Converter.dpToPx(BOTTOM_BAR_HEIGHT_DP) + Converter.dpToPx(FILTERS_LAYOUT_HEIGHT_DP) * (1f - animation.getAnimatedFraction())));
                }
            }
        }).start();
    }

    private SeekBar.OnSeekBarChangeListener mTimeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int minTime = 10 + (int) (4.70 * progress);
            int step = minTime < 60 ? 10 : 15;
            minTime = Math.round(minTime / step) * step;
            if (llFilter.getVisibility() == View.VISIBLE) mMapHolder.setMinTime(minTime);
            tvCurMinTime.setText(String.valueOf(minTime / 60) + "h " + String.valueOf(minTime % 60) + "m");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
