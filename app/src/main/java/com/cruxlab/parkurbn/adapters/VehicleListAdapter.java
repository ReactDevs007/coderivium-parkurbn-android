package com.cruxlab.parkurbn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.VehicleActivity;
import com.cruxlab.parkurbn.model.Vehicle;
import com.cruxlab.parkurbn.tools.Converter;

import java.util.LinkedList;
import java.util.List;

public class VehicleListAdapter extends BaseAdapter {

    private VehicleActivity mActivity;
    private List<Vehicle> mVehicles;
    private LayoutInflater mInflater;

    public VehicleListAdapter(VehicleActivity activity) {
        mActivity = activity;
        mVehicles = new LinkedList<>();
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mVehicles.size();
    }

    @Override
    public Object getItem(int position) {
        return mVehicles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(R.layout.item_vehicle, parent, false);
        }
        final Vehicle vehicle = mVehicles.get(position);
        view.findViewById(R.id.iv_arrow).setVisibility(mActivity.isEditMode() ? View.VISIBLE : View.GONE);
        if (vehicle.getNickname().isEmpty()) {
            ((TextView) view.findViewById(R.id.tv_nickname)).setText(vehicle.getLicensePlateNumber());
            ((TextView) view.findViewById(R.id.tv_plate)).setText("");
        } else {
            ((TextView) view.findViewById(R.id.tv_nickname)).setText(vehicle.getNickname());
            ((TextView) view.findViewById(R.id.tv_plate)).setText(vehicle.getLicensePlateNumber());
        }
        view.findViewById(R.id.iv_is_selected).setVisibility(!mActivity.isSelectMode() || mActivity.isEditMode() ? View.GONE : !mVehicles.get(position).getId().equals(mActivity.getSelectedVehicle().getId()) ? View.INVISIBLE : View.VISIBLE);
        ((TextView) view.findViewById(R.id.tv_nickname)).setMaxWidth(vehicle.isDefault() ? Converter.dpToPx(120) : Converter.dpToPx(300));
        view.findViewById(R.id.iv_default).setVisibility(vehicle.isDefault() ? View.VISIBLE : View.GONE);
        return view;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        mVehicles = vehicles;
        notifyDataSetChanged();
    }

    public void removeVehicle(int position) {
        boolean wasDefault = mVehicles.get(position).isDefault();
        mVehicles.remove(position);
        if (wasDefault && mVehicles.size() > 0) {
            mVehicles.get(0).setDefault(true);
        }
        notifyDataSetChanged();
    }

    public void setVehicle(int position, Vehicle newVehicle) {
        if (newVehicle.isDefault()) {
            for (Vehicle vehicle : mVehicles) {
                vehicle.setDefault(false);
            }
        }
        mVehicles.add(position, newVehicle);
        notifyDataSetChanged();
    }

}
