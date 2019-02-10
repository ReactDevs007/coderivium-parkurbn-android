package com.cruxlab.parkurbn.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.StateListActivity;

public class StateListAdapter extends BaseAdapter {

    private StateListActivity mActivity;
    private LayoutInflater mInflater;

    public StateListAdapter(StateListActivity activity) {
        mActivity = activity;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private String[] title = { "Alabama", "Alaska", "Arizona", "Arkansas", "Wyoming", "Washington", "Vermont", "Virginia", "Wisconsin", "Hawaii", "Delaware", "Georgia", "West Virginia", "Illinois", "Indiana", "California", "Kansas", "Kentucky", "Colorado", "Connecticut", "Louisiana", "Massachusetts", "Minnesota", "Mississippi", "Missouri", "Michigan", "Montana", "Maine", "Maryland", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New York", "New Mexico", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island and Providence Plantations", "North Dakota", "North Carolina", "Tennessee", "Texas", "Florida", "South Dakota", "South Carolina", "Utah"};
    private String[] id = {"AL", "AK", "AZ", "AR", "WY", "WA", "VT", "VA", "WI", "HI", "DE", "GA", "WV", "IL", "IN", "CA", "KS", "KY", "CO", "CT", "LA", "MA", "MN", "MS", "MO", "MI", "MT", "ME", "MD", "NE", "NV", "NH", "NJ", "NY", "NM", "OH", "OK", "OR", "PA", "RI", "ND", "NC", "TN", "TX", "FL", "SD", "SC", "UT"};

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public Object getItem(int position) {
        return new Pair<>(title[position], id[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_state, parent, false);
        }
        ((TextView) view.findViewById(R.id.tv_state_title)).setText(title[position]);
        ((TextView) view.findViewById(R.id.tv_state_id)).setText(id[position]);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.setStateResult(title[position], id[position]);
            }
        });
        return view;
    }
}
