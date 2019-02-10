package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Segment;

import butterknife.BindView;

public class InfoSegmentFragment extends BaseInfoFragment {

    @BindView(R.id.tv_av_spots)
    TextView tvAvSpots;
    @BindView(R.id.ll_av_spots)
    LinearLayout llAvSpots;

    private Segment mSegment;

    public static InfoSegmentFragment newInstance(Segment segment, Location location) {
        InfoSegmentFragment instance = new InfoSegmentFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(BundleArguments.SEGMENT, segment);
        arguments.putParcelable(BundleArguments.LOCATION, location);
        instance.setArguments(arguments);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        mSegment = getArguments().getParcelable(BundleArguments.SEGMENT);
        mCurLocation = getArguments().getParcelable(BundleArguments.LOCATION);
        updateInfo();
        llAvSpots.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void updateInfo() {
        getDistance();
        tvAvSpots.setText(String.valueOf(mSegment.getFreeSpots()));
        tvAddress.setText(mSegment.getAddress());
    }

    @Override
    public void setInfo(Object info) {
        mSegment = (Segment) info;
        getArguments().putParcelable(BundleArguments.SEGMENT, mSegment);
        updateInfo();
    }

    @Override
    public void getDistance() {
        if (mCurLocation != null) {
            getDistance(mCurLocation.toString(), mSegment.getLoc().toString());
        }
    }

}
