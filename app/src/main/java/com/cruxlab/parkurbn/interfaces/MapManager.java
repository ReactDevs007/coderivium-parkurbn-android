package com.cruxlab.parkurbn.interfaces;

import com.cruxlab.parkurbn.LocationManager;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public interface MapManager extends GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, LocationManager.LocationCallback {

    void onMapReady(MapHolder mapHolder);

    void onZoomChanged(float zoom);

    void onBearingChanged(float bearing);

    void onFreeSpotCntChanged(int freeCnt);

    void onMyLocationCentered(boolean centered);

    void onSpotSelected(Spot spot);

    void onSegmentSelected(Segment segment);

    void onSpotTaken(String id);

    void onSegmentChanged(String id, int count);

    void onCameraStoppedMoving(LatLng center);

    void onSpotNoLongerSatisfyFilter(String id);

    void onSegmentNoLongerSatisfyFilter(String id);

    void onSpotUnavailable(String id);

    void onSegmentUnavailable(String id);

    void onBackPressed();

}
