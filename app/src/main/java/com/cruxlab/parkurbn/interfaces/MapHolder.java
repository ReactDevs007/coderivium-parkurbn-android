package com.cruxlab.parkurbn.interfaces;

import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Set;

public interface MapHolder {

    void requestMap(MapManager mapManager);

    boolean isMapReady();

    void resetPrefs();

    int getMapMode();

    void setMapMode(int mode);

    void setBearing(float bearing);

    float getBearing();

    LatLngBounds getBounds();

    LatLng getCenter();

    void setMinZoom(float zoom);

    void setMaxZoom(float zoom);

    float getZoom();

    void setZoom(float zoom);

    void setZoom(float zoom, LatLng target);

    void setZoom(float zoom, GoogleMap.CancelableCallback cancelableCallback);

    void setZoom(float zoom, LatLng target, GoogleMap.CancelableCallback cancelableCallback);

    void showRoute(List<com.google.maps.model.LatLng> route);

    void showDestLoc(LatLng latLng);

    void showCar(LatLng latLng);

    void selectSpot(Spot spot);

    void deselectSpot();

    void selectSegment(Segment segment);

    void deselectSegment();

    void updateFreeSpotCnt();

    void requestRoadLocation();

    void requestGPSLocation();

    void setLocationUpdateFreq(long updateFreq);

    void setShowOnlyOneMarker(boolean showOnlyOneMarker);

    void showParkingTimeMarker(Spot parkedSpot);

    void addSearchMarker(LatLng latLng);

    void removeSearchMarker();

    void setPadding(int bottomPadding);

    void setMinTime(int time);

    int getMinTime();

    void adjustZoomForRoute();

    void clearMap();

    GoogleApiClient getGoogleApiClient();

    Location getCurrentRoadLocation();

    Location getCurrentLocation();

    void moveToLocation(LatLng latLng);

    void subscribeSpots(Set<String> spotIds);

    void subscribeSegments(Set<String> segmentIds);

    void takeMapScreenshot(GoogleMap.SnapshotReadyCallback callback);

    void setMyLocationButtonEnabled();

}
