package com.cruxlab.parkurbn.tools;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.cruxlab.parkurbn.R;

public class DialogUtils {

    public interface InfoDialogCallback {

        void okButtonPressed();

    }

    public interface ConfirmDialogCallback {

        void okButtonPressed();
        void cancelButtonPressed();

    }

    private static void showInfoDialog(Context context, int messageStrResId, final InfoDialogCallback callback) {
        new AlertDialog.Builder(context)
                .setMessage(messageStrResId)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.okButtonPressed();
                        }
                    }
                })
                .create()
                .show();
    }

    public static void showSpotIsTakenDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.the_spot_is_taken, callback);
    }

    public static void showNoFreeSpotsDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.there_are_no_free_spots, callback);
    }

    public static void showNoSpotsAroundDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.there_are_no_spots_around, callback);
    }

    public static void showSpotNoLongerSatisfyFilterDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.spot_no_longer_satisfy_filter, callback);
    }

    public static void showSegmentNoLongerSatisfyFilterDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.segment_no_longer_satisfy_filter, callback);
    }

    public static void showSpotIsUnavailableDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.spot_is_unavailable, callback);
    }

    public static void showSegmentIsUnavailableDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.segment_is_unavailable, callback);
    }

    public static void showParkingMeterExpiresSoonDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.parking_meter_expires_soon, callback);
    }

    public static void showSelectVehicleAndTimeDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.select_vehicle_and_time, callback);
    }

    public static void showNoRouteDialog(Context context, boolean isSpot, InfoDialogCallback callback) {
        showInfoDialog(context, isSpot ? R.string.no_route_to_spot : R.string.no_route_to_segment, callback);
    }

    public static void showSelectStateDialog(Context context, InfoDialogCallback callback) {
        showInfoDialog(context, R.string.select_state, callback);
    }

    private static void showConfirmDialog(Context context, int messageStrResId, int okStrResId, int cancelStrResId, final ConfirmDialogCallback callback) {
        new AlertDialog.Builder(context)
                .setMessage(messageStrResId)
                .setCancelable(false)
                .setPositiveButton(okStrResId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.okButtonPressed();
                        }
                    }
                })
                .setNegativeButton(cancelStrResId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.cancelButtonPressed();
                        }
                    }
                })
                .create()
                .show();
    }

    public static void showCapturedSpotIsTakenDialog(Context context, ConfirmDialogCallback callback) {
        showConfirmDialog(context, R.string.the_spot_is_taken, R.string.pay, R.string.find_another, callback);
    }

    public static void showDeleteVehicleDialog(Context context, ConfirmDialogCallback callback) {
        showConfirmDialog(context, R.string.are_you_sure_you_want_to_delete_vehicle, R.string.yes, R.string.no, callback);
    }

    public static void showAreYouSureDialog(Context context, ConfirmDialogCallback callback) {
        showConfirmDialog(context, R.string.are_you_sure, R.string.yes, R.string.no, callback);
    }

    public static void showLeaveParkingSpotDialog(Context context, ConfirmDialogCallback callback) {
        showConfirmDialog(context, R.string.do_you_want_to_leave_this_parking_spot, R.string.leave_parking, R.string.cancel, callback);
    }

    public static void showParkingMeterExpiresSoonDialog(Context context, ConfirmDialogCallback callback) {
        showConfirmDialog(context, R.string.parking_meter_expires_soon, R.string.refill, R.string.cancel, callback);
    }

}
