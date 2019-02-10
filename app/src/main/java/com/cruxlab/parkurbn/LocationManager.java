package com.cruxlab.parkurbn;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.model.Location;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.SnappedPoint;

/*
 * The main purpose of this class is to separate all location-related logic.
 * For now it is used only in MapActivity which passes current location to all overlay fragments.
 * Current location can be requested by calling requestGPSLocation() or requestRoadLocation() and then handling LocationCallback.
 * LocationManager provides only one type of location data at a time.
 * When road location is unavailable, LoginManager triggers LocationCallback passing gps location.
 * When something goes wrong, LocationManager triggers onLocationUnavailable() (e.g. location restricted or snapToRoad query failed).
 * When location is permitted, but not available yet, LocationManager triggers onLocationPermitted() (e.g. overlay fragments show loader).
 * When requested location is available, LoginManager triggers onLocationGranted() passing current location.
 * OnLocationChanged() is triggered every time new location is available.
 */

public class LocationManager implements LocationListener {

    public interface LocationCallback {

        void onLocationPermitted();

        void onLocationUnavailable();

        void onLocationGranted(Location location);

        void onLocationChanged(Location location);

    }
    private static final int NONE = 0;
    private static final int GPS_LOCATION = 1;
    private static final int ROAD_LOCATION = 2;

    public static final long DEFAULT_UPDATE_FREQ = 1000 * 5;
    public static final long FAST_UPDATE_FREQ = 1000;

    private Activity mActivity;
    private LocationCallback mCallback;
    private GoogleApiClient mGoogleApiClient;
    private GeoApiContext mGeoApiContext;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder mBuilder;
    private android.location.LocationManager mLocationManager;

    private Location lastKnownRoadLocation;
    private Location lastKnownLocation;
    private float lastKnownBearing;

    private boolean isLocationRequested;
    private int locationType = NONE;

    public LocationManager(GoogleApiClient googleApiClient, Activity activity) {
        mActivity = activity;
        mCallback = (LocationCallback) activity;
        mGoogleApiClient = googleApiClient;
        mGeoApiContext = ParkUrbnApplication.get().getGeoApiContext();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(DEFAULT_UPDATE_FREQ);
        mLocationRequest.setFastestInterval(DEFAULT_UPDATE_FREQ);
        mBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        mBuilder.setAlwaysShow(true);
        mLocationManager = (android.location.LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        lastKnownBearing = location.getBearing();
        lastKnownLocation = new Location(location);
        if (locationType == GPS_LOCATION) {
            notifyLocationAvailable(lastKnownLocation);
        } else if (locationType == ROAD_LOCATION) {
            snapToRoad(location);
        }
    }

    public void startLocationUpdates() {
        if (!mGoogleApiClient.isConnected()) return;
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.LOCATION_PERMISSION);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void stopLocationUpdates() {
        if (!mGoogleApiClient.isConnected()) return;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public Location getLastKnownRoadLocation() {
        if (!mLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            lastKnownRoadLocation = null;
            locationType = NONE;
        }
        return lastKnownRoadLocation;
    }

    public Location getLastKnownLocation() {
        if (!mLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            lastKnownLocation = null;
            locationType = NONE;
        }
        return lastKnownLocation;
    }

    public float getLastKnownBearing() {
        return lastKnownBearing;
    }

    public void requestRoadLocation() {
        if (isLocationRequested) return;
        isLocationRequested = true;
        locationType = ROAD_LOCATION;
        checkPermissions();
    }

    public void requestGPSLocation() {
        if (isLocationRequested) return;
        isLocationRequested = true;
        locationType = GPS_LOCATION;
        checkPermissions();
    }

    public void setUpdateFreq(long updateFreq) {
        mLocationRequest.setInterval(updateFreq);
        mLocationRequest.setFastestInterval(updateFreq);
        stopLocationUpdates();
        startLocationUpdates();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.LOCATION_PERMISSION) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationRequested) {
                    checkLocationSettings();
                } else {
                    startLocationUpdates();
                }
            } else {
                notifyLocationUnavailable();
            }
        }
    }

    public void notifyLocationPermitted() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.LOCATION_PERMISSION);
        } else {
            android.location.Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                lastKnownBearing = location.getBearing();
                lastKnownLocation = new Location(location);
                if (locationType == GPS_LOCATION) {
                    notifyLocationAvailable(lastKnownLocation);
                } else if (locationType == ROAD_LOCATION) {
                    snapToRoad(location);
                }
            } else {
                if (mCallback != null) {
                    mCallback.onLocationPermitted();
                }
            }
        }
    }

    private void notifyLocationAvailable(Location location) {
        if (mCallback != null) {
            if (isLocationRequested) {
                mCallback.onLocationGranted(location);
            } else {
                mCallback.onLocationChanged(location);
            }
        }
        isLocationRequested = false;
    }

    public void notifyLocationUnavailable() {
        if (mCallback != null) {
            mCallback.onLocationUnavailable();
        }
        isLocationRequested = false;
    }

    private void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        notifyLocationPermitted();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(mActivity, RequestCodes.REQUEST_GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            notifyLocationUnavailable();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        notifyLocationUnavailable();
                }
            }
        });
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.LOCATION_PERMISSION);
        } else {
            checkLocationSettings();
        }
    }

    private void snapToRoad(final android.location.Location location) {
        com.google.maps.model.LatLng latLng = new com.google.maps.model.LatLng(location.getLatitude(), location.getLongitude());
        RoadsApi.snapToRoads(mGeoApiContext, latLng).setCallback(new com.google.maps.PendingResult.Callback<SnappedPoint[]>() {
            @Override
            public void onResult(final SnappedPoint[] result) {
                if (result != null && result.length > 0) {
                    final LatLng latLng = new LatLng(result[0].location.lat, result[0].location.lng);
                    lastKnownRoadLocation = new Location(latLng);
                    notifyLocationAvailable(lastKnownRoadLocation);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                lastKnownRoadLocation = null;
                if (lastKnownLocation != null) {
                    notifyLocationAvailable(lastKnownLocation);
                } else {
                    notifyLocationUnavailable();
                }
            }
        });

    }

}
