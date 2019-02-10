package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.model.Location;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Unit;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseInfoFragment extends BaseFragment {

    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.tv_duration)
    TextView tvDuration;

    protected GeoApiContext mGeoApiContext;
    protected Location mCurLocation;
    private OnRouteExistListener mOnRouteExistListener;

    private int routeExists = -1;

    protected interface OnRouteExistListener {
        void onRouteExists(boolean exists);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.info_layout, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        mGeoApiContext = new GeoApiContext().setApiKey(getString(R.string.google_maps_key));
        return rootView;
    }

    protected abstract void setInfo(Object info);
    protected abstract void getDistance();

    protected void getDistance(String curLocation, String dest) {
        DistanceMatrixApi.getDistanceMatrix(mGeoApiContext, new String[]{curLocation}, new String[]{dest})
                .units(Unit.METRIC)
                .setCallback(mDistanceMatrixCallback);
    }

    protected PendingResult.Callback<DistanceMatrix> mDistanceMatrixCallback = new PendingResult.Callback<DistanceMatrix>() {
        @Override
        public void onResult(DistanceMatrix result) {
            DistanceMatrixRow[] rows = result.rows;
            if (rows.length > 0) {
                final DistanceMatrixElement[] elements = rows[0].elements;
                if (elements.length > 0) {
                    if (mUnbinder != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mUnbinder != null) {
                                    if (elements[0].duration != null) {
                                        tvDuration.setText(elements[0].duration.humanReadable);
                                        tvDuration.setVisibility(View.VISIBLE);
                                        routeExists = 1;
                                        if (mOnRouteExistListener != null) {
                                            mOnRouteExistListener.onRouteExists(true);
                                        }
                                    } else hideDuration();
                                }
                            }
                        });
                    }
                } else hideDuration();
            } else hideDuration();
        }

        @Override
        public void onFailure(final Throwable e) {
            e.printStackTrace();
            if (mUnbinder != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mUnbinder != null) {
                            hideDuration();
                        }
                    }
                });
            }
        }
    };

    private void hideDuration() {
        tvDuration.setVisibility(View.GONE);
        routeExists = 0;
        if (mOnRouteExistListener != null) {
            mOnRouteExistListener.onRouteExists(false);
        }
    }

    public int isRouteExists() {
        return routeExists;
    }

    public void setOnRouteExistListener(OnRouteExistListener listener) {
        mOnRouteExistListener = listener;
    }

    public void setCurLocation(Location location) {
        mCurLocation = location;
        getDistance();
    }

}
