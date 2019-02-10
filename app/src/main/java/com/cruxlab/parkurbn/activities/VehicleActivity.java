package com.cruxlab.parkurbn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.fragments.VehicleFragment;
import com.cruxlab.parkurbn.fragments.VehicleListFragment;
import com.cruxlab.parkurbn.model.Vehicle;

import butterknife.ButterKnife;

public class VehicleActivity extends BaseActivity {

    private ParkUrbnApi mParkUrbnApi;

    private boolean isEditMode;
    private boolean isSelectMode;

    private Vehicle mSelectedVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);
        ButterKnife.bind(this);
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        mSelectedVehicle = getIntent().getParcelableExtra(BundleArguments.VEHICLE);
        if (savedInstanceState == null) {
            isSelectMode = getIntent().getBooleanExtra(BundleArguments.SELECT_MODE, false);
            isEditMode = !isSelectMode;
            if (isSetupMode()) {
                setContentFragment(VehicleFragment.newInstance(true), true);
            } else {
                setContentFragment(VehicleListFragment.newInstance(), true);
            }
        } else {
            isSelectMode = savedInstanceState.getBoolean(BundleArguments.SELECT_MODE);
            isEditMode = savedInstanceState.getBoolean(BundleArguments.EDIT_MODE);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            setVehicleResult();
        } else {
            super.onBackPressed();
        }
    }

    public ParkUrbnApi getParkUrbnApi() {
        return mParkUrbnApi;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BundleArguments.EDIT_MODE, isEditMode);
        outState.putBoolean(BundleArguments.SELECT_MODE, isSelectMode);
    }

    public void setContentFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getSupportFragmentManager().findFragmentById(R.id.content_vehicle) == null) {
            transaction.add(R.id.content_vehicle, fragment);
        } else {
            transaction.replace(R.id.content_vehicle, fragment);
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
        }
        transaction.commit();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public boolean isSelectMode() {
        return isSelectMode;
    }

    public boolean isSetupMode() {
        return isSelectMode && mSelectedVehicle == null;
    }

    public void setIsEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public void setSelectedVehicle(Vehicle vehicle) {
        mSelectedVehicle = vehicle;
    }

    public Vehicle getSelectedVehicle() {
        return mSelectedVehicle;
    }

    public void setVehicleResult() {
        Intent intent = new Intent();
        intent.putExtra(BundleArguments.VEHICLE, mSelectedVehicle);
        setResult(RESULT_OK, intent);
        finish();
    }

}
