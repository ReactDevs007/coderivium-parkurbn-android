package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Spot;

import butterknife.BindView;

public class InfoSpotFragment extends BaseInfoFragment {

    @BindView(R.id.ll_address)
    LinearLayout llAddress;

    private Spot mSpot;

    public static InfoSpotFragment newInstance(Spot spot, Location location) {
        InfoSpotFragment instance = new InfoSpotFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(BundleArguments.SPOT, spot);
        arguments.putParcelable(BundleArguments.LOCATION, location);
        instance.setArguments(arguments);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        mSpot = getArguments().getParcelable(BundleArguments.SPOT);
        mCurLocation = getArguments().getParcelable(BundleArguments.LOCATION);
        llAddress.setVisibility(View.VISIBLE);
        updateInfo();
        return rootView;
    }

    private void updateInfo() {
        getDistance();
        tvAddress.setText(mSpot.getAddress());
    }

    @Override
    public void setInfo(Object info) {
        mSpot = (Spot) info;
        getArguments().putParcelable(BundleArguments.SPOT, mSpot);
        updateInfo();
    }

    @Override
    protected void getDistance() {
        if (mCurLocation != null) {
            getDistance(mCurLocation.toString(), mSpot.getLoc().toString());
        }
    }

}
