package com.cruxlab.parkurbn.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.activities.VehicleActivity;
import com.cruxlab.parkurbn.adapters.VehicleListAdapter;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.custom.SwipeDismissListViewTouchListener;
import com.cruxlab.parkurbn.model.Vehicle;
import com.cruxlab.parkurbn.model.request.DeleteVehicleBody;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.tools.DialogUtils;
import com.google.gson.JsonObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class VehicleListFragment extends BaseFragment {

    @BindView(R.id.btn_action)
    Button btnAction;
    @BindView(R.id.lv_vehicles)
    ListView lvVehicles;
    @BindView(R.id.ll_vehicles)
    LinearLayout llVehicles;
    @BindView(R.id.ll_no_vehicles)
    LinearLayout llNoVehicles;

    private VehicleListAdapter mAdapter;

    public static VehicleListFragment newInstance() {
        return new VehicleListFragment();
    }

    /* LIFECYCLE */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new VehicleListAdapter(getVehicleActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_vehicle_list, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        if (!getVehicleActivity().isSelectMode()) {
            getVehicleActivity().setIsEditMode(true);
        } else if (getVehicleActivity().isEditMode()) {
            enterEditMode();
        }
        lvVehicles.setAdapter(mAdapter);
        View footerView = inflater.inflate(R.layout.footer_vehicle_list, null);
        footerView.findViewById(R.id.ll_select_vehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVehicleActivity().setIsEditMode(false);
                getVehicleActivity().setContentFragment(VehicleFragment.newInstance(mAdapter.getCount() == 0), true);
            }
        });
        lvVehicles.addFooterView(footerView);
        getVehicles();
        lvVehicles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Vehicle vehicle = (Vehicle) parent.getAdapter().getItem(position);
                if (getVehicleActivity().isEditMode()) {
                    getVehicleActivity().setContentFragment(VehicleFragment.newInstance(vehicle, mAdapter.getCount() == 1), true);
                } else {
                    getVehicleActivity().setSelectedVehicle(vehicle);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        SwipeDismissListViewTouchListener listItemTouchListener =
                new SwipeDismissListViewTouchListener(
                        lvVehicles,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {

                            @Override
                            public boolean canDismiss(int position) {
                                return getVehicleActivity().isEditMode();
                            }

                            @Override
                            public void onDismiss(ListView listView, final int[] reverseSortedPositions) {
                                if (reverseSortedPositions.length == 0) return;
                                final int position = reverseSortedPositions[0];
                                final Vehicle vehicle = (Vehicle) mAdapter.getItem(position);
                                mAdapter.removeVehicle(position);
                                DialogUtils.showDeleteVehicleDialog(getActivity(), new DialogUtils.ConfirmDialogCallback() {
                                    @Override
                                    public void okButtonPressed() {
                                        deleteVehicle(position, vehicle);
                                    }

                                    @Override
                                    public void cancelButtonPressed() {
                                        mAdapter.setVehicle(position, vehicle);
                                    }
                                });
                            }
                        });
        lvVehicles.setOnTouchListener(listItemTouchListener);
        lvVehicles.setOnScrollListener(listItemTouchListener.makeScrollListener());
        footerView.setOnClickListener(null);
        footerView.setOnTouchListener(null);
        return rootView;
    }

    /* END LIFECYCLE */
    /* ON CLICK */

    @OnClick(R.id.btn_action)
    void changeEditMode() {
        if (getVehicleActivity().isEditMode()) {
            exitEditMode();
        } else {
            enterEditMode();
        }
    }

    @OnClick(R.id.ibtn_nav_btn)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.btn_add)
    void addVehicle() {
        getVehicleActivity().setIsEditMode(false);
        getVehicleActivity().setContentFragment(VehicleFragment.newInstance(mAdapter.getCount() == 0), true);
    }

    private void onBackPressed() {
        if (getVehicleActivity().isSelectMode() && getVehicleActivity().isEditMode()) {
            exitEditMode();
        } else {
            getActivity().onBackPressed();
        }
    }

    /* END ON CLICK */
    /* QUERIES */

    private void getVehicles() {
        if (mAdapter.getCount() == 0) {
            getVehicleActivity().showLoader();
        }
        getVehicleActivity().getParkUrbnApi().getVehicleList().enqueue(new ResponseCallback<List<Vehicle>>() {
            @Override
            public void handleResponse(Response<List<Vehicle>> response) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    List<Vehicle> vehicles = response.body();
                    mAdapter.setVehicles(vehicles);
                    if (getVehicleActivity().getSelectedVehicle() == null && vehicles.size() > 0) {
                        getVehicleActivity().setSelectedVehicle(vehicles.get(0));
                    }
                    if (vehicles.size() == 0) {
                        handleEmptyList();
                    } else {
                        llVehicles.setVisibility(View.VISIBLE);
                        llNoVehicles.setVisibility(View.GONE);
                        if (getVehicleActivity().isSelectMode()) {
                            btnAction.setVisibility(View.VISIBLE);
                        }
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
            public void onFailure(Call<List<Vehicle>> call, Throwable t) {
                if (mUnbinder != null) {
                    getVehicleActivity().hideLoader();
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteVehicle(final int position, final Vehicle vehicle) {
        getVehicleActivity().getParkUrbnApi().deleteVehicle(new DeleteVehicleBody(vehicle.getId())).enqueue(new ResponseCallback<JsonObject>() {
            @Override
            public void handleResponse(Response<JsonObject> response) {
                if (getVehicleActivity().getSelectedVehicle() != null && getVehicleActivity().getSelectedVehicle().getId().equals(vehicle.getId())) {
                    if (mUnbinder != null) {
                        if (mAdapter.getCount() == 0) {
                            getVehicleActivity().setSelectedVehicle(null);
                            handleEmptyList();
                        } else {
                            getVehicleActivity().setSelectedVehicle((Vehicle) mAdapter.getItem(0));
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                if (mUnbinder != null) {
                    Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                super.onFailure(call, t);
                if (mUnbinder != null) {
                    Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_SHORT).show();
                    mAdapter.setVehicle(position, vehicle);
                }
            }

        });
    }

    /* END QUERIES */

    private VehicleActivity getVehicleActivity() {
        return (VehicleActivity) getActivity();
    }

    private void enterEditMode() {
        btnAction.setText(getString(R.string.save));
        getVehicleActivity().setIsEditMode(true);
        mAdapter.notifyDataSetChanged();
    }

    private void exitEditMode() {
        btnAction.setText(getString(R.string.edit));
        getVehicleActivity().setIsEditMode(false);
        mAdapter.notifyDataSetChanged();
    }

    private void handleEmptyList() {
        if (!getVehicleActivity().isSelectMode()) {
            llVehicles.setVisibility(View.GONE);
            llNoVehicles.setVisibility(View.VISIBLE);
            btnAction.setVisibility(View.GONE);
        } else {
            getVehicleActivity().setIsEditMode(false);
            getVehicleActivity().setSelectedVehicle(null);
            getVehicleActivity().setContentFragment(VehicleFragment.newInstance(mAdapter.getCount() == 0), false);
        }
    }

}
