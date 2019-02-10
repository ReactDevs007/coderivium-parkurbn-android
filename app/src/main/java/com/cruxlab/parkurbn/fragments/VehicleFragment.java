package com.cruxlab.parkurbn.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.StateListActivity;
import com.cruxlab.parkurbn.activities.VehicleActivity;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.model.Vehicle;
import com.cruxlab.parkurbn.model.request.CreateVehicleBody;
import com.cruxlab.parkurbn.model.request.DeleteVehicleBody;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.tools.Converter;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.cruxlab.parkurbn.tools.ValidatorUtils;
import com.google.gson.JsonObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class VehicleFragment extends BaseFragment {

    @BindView(R.id.tv_nav_title)
    TextView tvNavTitle;
    @BindView(R.id.et_vehicle_title)
    TextInputLayout etNickname;
    @BindView(R.id.et_vehicle_plate)
    TextInputLayout etPlate;
    @BindView(R.id.sw_is_vehicle_default)
    Switch isDefault;
    @BindView(R.id.btn_remove)
    Button btnRemove;
    @BindView(R.id.tv_order_modify_text)
    TextView tvOrderModifyText;
    @BindView(R.id.tv_state_title)
    TextView tvStateTitle;
    @BindView(R.id.tv_state_id)
    TextView tvStateId;
    @BindView(R.id.tv_first_vehicle_text)
    TextView tvFirstVehicleText;
    @BindView(R.id.tv_first_default_vehicle)
    TextView tvDefaultText;

    private Vehicle mVehicle;

    boolean isOnly;

    public static VehicleFragment newInstance(boolean isOnly) {
        VehicleFragment instance = new VehicleFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(BundleArguments.IS_ONLY, isOnly);
        instance.setArguments(arguments);
        return instance;
    }

    public static VehicleFragment newInstance(Vehicle vehicle, boolean isOnly) {
        VehicleFragment instance = new VehicleFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(BundleArguments.VEHICLE, vehicle);
        arguments.putBoolean(BundleArguments.IS_ONLY, isOnly);
        instance.setArguments(arguments);
        return instance;
    }

    /* LIFECYCLE */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_vehicle, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        isOnly = getArguments().getBoolean(BundleArguments.IS_ONLY);
        if (isOnly) {
            isDefault.setEnabled(false);
            tvDefaultText.setVisibility(View.VISIBLE);
            if (!getVehicleActivity().isEditMode()) {
                tvFirstVehicleText.setVisibility(View.VISIBLE);
            }
        }
        if (getArguments() != null) {
            mVehicle = getArguments().getParcelable(BundleArguments.VEHICLE);
        }
        initViews();
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SELECT_STATE) {
            if (resultCode == Activity.RESULT_OK) {
                String title = data.getStringExtra(BundleArguments.STATE_TITLE);
                String id = data.getStringExtra(BundleArguments.STATE_ID);
                tvStateTitle.setText(title);
                tvStateId.setText(id);
            }
        }
    }

    /* END LIFECYCLE */
    /* ON CLICK */

    @OnClick(R.id.btn_action)
    void save() {
        boolean isValid = ValidatorUtils.checkNickname(getActivity(), etNickname, etNickname.getEditText().getText().toString());
        isValid &= ValidatorUtils.checkLicensePlateNumber(getActivity(), etPlate, etPlate.getEditText().getText().toString());
        if (isValid) {
            if (tvStateId.getText().toString().equals(getString(R.string.select))) {
                DialogUtils.showSelectStateDialog(getActivity(), null);
            } else {
                if (getVehicleActivity().isEditMode()) {
                    changeVehicle();
                } else {
                    addVehicle();
                }
            }
        }
    }

    @OnClick(R.id.btn_cancel)
    void back() {
        getVehicleActivity().hideKeyboard();
        getActivity().onBackPressed();
    }

    @OnClick(R.id.btn_remove)
    void removeVehicle() {
        DialogUtils.showDeleteVehicleDialog(getActivity(), new DialogUtils.ConfirmDialogCallback() {
            @Override
            public void okButtonPressed() {
                deleteVehicle();
            }

            @Override
            public void cancelButtonPressed() {

            }
        });
    }

    @OnClick(R.id.rl_select_state)
    void selectState() {
        startActivityForResult(new Intent(getActivity(), StateListActivity.class), RequestCodes.SELECT_STATE);
    }

    /* END ON CLICK */
    /* QUERIES */

    private void addVehicle() {
        getVehicleActivity().showLoader();
        getVehicleActivity().getParkUrbnApi().createVehicle(new CreateVehicleBody(etNickname.getEditText().getText().toString(),
                etPlate.getEditText().getText().toString(), isDefault.isChecked(), tvStateId.getText().toString())).enqueue(new ResponseCallback<Vehicle>() {
            @Override
            public void handleResponse(Response<Vehicle> response) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    getVehicleActivity().hideKeyboard();
                    getVehicleActivity().setSelectedVehicle(response.body());
                    if (getVehicleActivity().isSetupMode()) {
                        getVehicleActivity().setVehicleResult();
                    } else {
                        getVehicleActivity().onBackPressed();
                    }
                }
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Vehicle> call, Throwable t) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void changeVehicle() {
        getVehicleActivity().showLoader();
        mVehicle.setNickname(etNickname.getEditText().getText().toString());
        mVehicle.setLicensePlateNumber(etPlate.getEditText().getText().toString());
        mVehicle.setDefault(isDefault.isChecked());
        mVehicle.setState(tvStateId.getText().toString());
        getVehicleActivity().getParkUrbnApi().changeVehicle(mVehicle).enqueue(new ResponseCallback<Vehicle>() {
            @Override
            public void handleResponse(Response<Vehicle> response) {
                if (mUnbinder != null) {
                    if (getVehicleActivity().getSelectedVehicle().getId().equals(mVehicle.getId())) {
                        getVehicleActivity().setSelectedVehicle(response.body());
                    }
                    getVehicleActivity().hideLoader();
                    getVehicleActivity().hideKeyboard();
                    getActivity().onBackPressed();
                }
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Vehicle> call, Throwable t) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteVehicle() {
        getVehicleActivity().showLoader();
        getVehicleActivity().getParkUrbnApi().deleteVehicle(new DeleteVehicleBody(mVehicle.getId())).enqueue(new ResponseCallback<JsonObject>() {
            @Override
            public void handleResponse(Response<JsonObject> response) {
                if (mUnbinder != null) {
                    if (getVehicleActivity().getSelectedVehicle().getId().equals(mVehicle.getId())) {
                        getVehicleActivity().setSelectedVehicle(null);
                    }
                    getVehicleActivity().hideLoader();
                    getVehicleActivity().hideKeyboard();
                    getActivity().onBackPressed();
                }
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                super.onFailure(call, t);
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    /* END QUERIES */

    private VehicleActivity getVehicleActivity() {
        return (VehicleActivity) getActivity();
    }

    private void initEditFields() {
        tvNavTitle.setText(getString(R.string.edit_vehicle));
        etNickname.getEditText().setText(mVehicle.getNickname());
        etPlate.getEditText().setText(mVehicle.getLicensePlateNumber());
        tvStateId.setText(mVehicle.getState());
        tvStateTitle.setText(Converter.stateIdToTitle(mVehicle.getState()));
        isDefault.setChecked(mVehicle.isDefault());
        btnRemove.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        if (getVehicleActivity().isEditMode()) {
            initEditFields();
        } else {
            if (getVehicleActivity().isSetupMode()) {
                tvNavTitle.setText(getString(R.string.select_your_vehicle));
            } else {
                tvNavTitle.setText(getString(R.string.add_vehicle));
            }
            tvOrderModifyText.setVisibility(View.VISIBLE);
        }
    }

}
