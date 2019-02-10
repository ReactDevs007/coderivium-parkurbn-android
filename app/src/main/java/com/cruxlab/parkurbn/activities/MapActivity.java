package com.cruxlab.parkurbn.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cruxlab.parkurbn.LocationManager;
import com.cruxlab.parkurbn.ParkUrbnApplication;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.SharedPrefsManager;
import com.cruxlab.parkurbn.adapters.MenuAdapter;
import com.cruxlab.parkurbn.api.InfoUpdateManager;
import com.cruxlab.parkurbn.api.ParkUrbnApi;
import com.cruxlab.parkurbn.api.ResponseCallback;
import com.cruxlab.parkurbn.consts.BundleArguments;
import com.cruxlab.parkurbn.consts.MapModes;
import com.cruxlab.parkurbn.consts.RequestCodes;
import com.cruxlab.parkurbn.consts.ZoomLevels;
import com.cruxlab.parkurbn.fragments.OverlayCapturedFragment;
import com.cruxlab.parkurbn.fragments.OverlayOverviewFragment;
import com.cruxlab.parkurbn.fragments.OverlayParkedFragment;
import com.cruxlab.parkurbn.interfaces.MapHolder;
import com.cruxlab.parkurbn.interfaces.MapManager;
import com.cruxlab.parkurbn.model.Location;
import com.cruxlab.parkurbn.model.Segment;
import com.cruxlab.parkurbn.model.Spot;
import com.cruxlab.parkurbn.model.request.MapRequest;
import com.cruxlab.parkurbn.model.response.ErrorResponse;
import com.cruxlab.parkurbn.tools.BitmapUtils;
import com.cruxlab.parkurbn.tools.Converter;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, InfoUpdateManager.IInfoUpdate,
        GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraIdleListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnPolylineClickListener, MapHolder, AdapterView.OnItemClickListener, LocationManager.LocationCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    @BindView(R.id.lv_menu)
    ListView lvMenu;
    @BindView(R.id.tv_sign_out)
    TextView tvSignOut;
    @BindView(R.id.tv_version)
    TextView tvVersion;

    @BindColor(R.color.color_blue)
    int colorBlue;
    @BindColor(R.color.color_segment_green)
    int colorSegmentGreen;
    @BindColor(R.color.color_segment_yellow)
    int colorSegmentYellow;
    @BindColor(R.color.color_segment_red)
    int colorSegmentRed;

    private static final String TAG = MapActivity.class.getSimpleName();
    private static final double EARTH_RADIUS = 6372795;
    public static final int DEFAULT_ANIM_TIME = 700;
    public static final int COMPASS_ANIM_TIME = 300;
    public static final String SCREENSHOT_NAME = "map_screenshot";
    public static final long TIMER_STEP = 1000L;

    private GoogleMap mMap;
    private MapManager mMapManager;
    private ParkUrbnApi mParkUrbnApi;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private InfoUpdateManager mInfoUpdateManager;

    private int mapMode;
    private boolean showOnlyOneMarker;
    private int minTime = 10;
    private int spotMarkerSizePx, circleMarkerSizePx;
    private boolean shouldClearMap;

    private ConcurrentHashMap<String, Marker> mMarkers;
    private ConcurrentHashMap<String, Polyline> mPolylines;
    private ConcurrentHashMap<String, Marker> mCircleMarkers;
    private Polyline mSelectedSegmentBorderPolyline;
    private Marker mCar, mSearchMarker, mTimeMarker, mDestMarker;

    private HashMap<String, Spot> mSpots;
    private HashMap<String, Segment> mSegments;
    private Spot mSelectedSpot;
    private Segment mSelectedSegment;

    private BitmapDescriptor mDestinationBD;
    private BitmapDescriptor[] mFreeBD, mTakenBD, mSelectedBD;
    private BitmapDescriptor mFreeCircleBD, mTakenCircleBD, mSelectedCircleBD;
    private Bitmap mFreeCircleBitmap, mSelectedCircleBitmap, mTakenCircleBitmap;
    private Bitmap[] mFreeBitmap, mTakenBitmap, mSelectedBitmap;
    private Bitmap mCarBitmap;

    private CountDownTimer mCountDownTimer;

    /* LIFECYCLE */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        setupDrawer();
        initOverlayFragment();
        initFields();
        initData();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.startLocationUpdates();
        mInfoUpdateManager.connectToSockets(mSpots.keySet(), mSegments.keySet());
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.stopLocationUpdates();
        mInfoUpdateManager.disconnectFromSockets();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
    /* END LIFECYCLE */
    /* ON CLICK */

    @OnClick(R.id.tv_sign_out)
    void signOut() {
        tvSignOut.setOnClickListener(null);
        logout();
    }

    /* END ON CLICK */
    /* INITIALIZATION */

    private void initFields() {
        mSpots = new HashMap<>();
        mSegments = new HashMap<>();
        mMarkers = new ConcurrentHashMap<>();
        mPolylines = new ConcurrentHashMap<>();
        mCircleMarkers = new ConcurrentHashMap<>();
        mFreeBD = new BitmapDescriptor[3];
        mTakenBD = new BitmapDescriptor[3];
        mSelectedBD = new BitmapDescriptor[3];
        mFreeBitmap = new Bitmap[3];
        mTakenBitmap = new Bitmap[3];
        mSelectedBitmap = new Bitmap[3];
    }

    private void initBitmapDescriptors() {
        mFreeBitmap[0] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_marker);
        mTakenBitmap[0] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_off_marker);
        mSelectedBitmap[0] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_selected_marker);
        mFreeBitmap[1] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_electro_marker);
        mTakenBitmap[1] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_electro_off_marker);
        mSelectedBitmap[1] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_electro_selected_marker);
        mFreeBitmap[2] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_disabled_marker);
        mTakenBitmap[2] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_disabled_off_marker);
        mSelectedBitmap[2] = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_disabled_selected_marker);
        mCarBitmap = BitmapUtils.fromDrawable(getResources(), R.drawable.ic_car);
        mDestinationBD = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(BitmapUtils.fromDrawable(getResources(), R.drawable.ic_parking_selected_marker), Converter.dpToPx(50), Converter.dpToPx(50)));
        mFreeCircleBitmap = BitmapUtils.fromVectorDrawable(this, R.drawable.marker_circle_free);
        mSelectedCircleBitmap = BitmapUtils.fromVectorDrawable(this, R.drawable.marker_circle_selected);
        mTakenCircleBitmap = BitmapUtils.fromVectorDrawable(this, R.drawable.marker_circle_taken);
    }

    private void initOverlayFragment() {
        Spot capturedSpot = SharedPrefsManager.get().getCapturedSpot();
        Segment capturedSegment = SharedPrefsManager.get().getCapturedSegment();
        Spot parkedSpot = SharedPrefsManager.get().getParkedSpot();
        if (parkedSpot != null) {
            setOverlayFragment(OverlayParkedFragment.newInstance(), false);
            setLockDrawer(true);
        } else if (capturedSpot != null || capturedSegment != null) {
            setOverlayFragment(OverlayCapturedFragment.newInstance(), false);
        } else {
            showLoader();
            setOverlayFragment(OverlayOverviewFragment.newInstance(), false);
        }
    }

    void initData() {
        mParkUrbnApi = ParkUrbnApplication.get().getParkUrbnApi();
        mGoogleApiClient = new GoogleApiClient.Builder(MapActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mLocationManager = new LocationManager(mGoogleApiClient, MapActivity.this);
        mInfoUpdateManager = new InfoUpdateManager(this);
    }

    private void initMap() {
        if (!mMap.getUiSettings().isCompassEnabled()) return;
        initCenterLatLng();
        mMap.setMaxZoomPreference(ZoomLevels.DEFAULT_MAX);
        mMap.setMinZoomPreference(ZoomLevels.DEFAULT_MIN);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        initBitmapDescriptors();
    }

    private void initCenterLatLng() {
        LatLng latLng = new LatLng(34.016, -118.499);
        Spot capturedSpot = SharedPrefsManager.get().getCapturedSpot();
        Segment capturedSegment = SharedPrefsManager.get().getCapturedSegment();
        Spot parkingSpot = SharedPrefsManager.get().getParkedSpot();
        if (parkingSpot != null) {
            latLng = parkingSpot.getLatLng();
        } else if (capturedSegment != null) {
            latLng = capturedSegment.getCentralSpot();
        } else if (capturedSpot != null) {
            latLng = capturedSpot.getLatLng();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZoomLevels.DEFAULT_MIN));
    }

    /* END INITIALIZATION */
    /* EVENTS */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id == R.string.my_account) {
            startActivity(new Intent(MapActivity.this, AccountActivity.class));
        } else if (id == R.string.vehicles) {
            startActivity(new Intent(MapActivity.this, VehicleActivity.class));
        } else if (id == R.string.parking_history) {
            startActivity(new Intent(MapActivity.this, ParkingHistoryActivity.class));
        } else if (id == R.string.feedback) {
            startActivity(new Intent(MapActivity.this, FeedbackActivity.class));
        } else if (id == R.string.manhattan_beach) {
            setZoom(ZoomLevels.DEFAULT_MIN, new LatLng(33.884, -118.410));
        } else if (id == R.string.berkeley) {
            setZoom(ZoomLevels.DEFAULT_MIN, new LatLng(37.869, -122.268));
        } else if (id == R.string.seattle) {
            setZoom(ZoomLevels.DEFAULT_MIN, new LatLng(47.666, -122.383));
        } else if (id == R.string.santa_monica) {
            setZoom(ZoomLevels.DEFAULT_MIN, new LatLng(34.016, -118.499));
        }
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.REQUEST_GPS_SETTINGS) {
            if (resultCode == RESULT_OK) {
                mLocationManager.notifyLocationPermitted();
            } else {
                mLocationManager.notifyLocationUnavailable();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mMapManager != null) {
                mMapManager.onBackPressed();
            } else {
                superOnBackPressed();
            }
        }
    }

    /* END EVENTS */
    /* UPDATE INFO CALLBACKS */

    @Override
    public void onSpotStateChanged(final String id, final boolean isTaken) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSpots.containsKey(id)) {
                    final Spot spot = mSpots.get(id);
                    spot.setIsTaken(isTaken);
                    mSpots.put(id, spot);
                    updateSpotState(spot);
                }
                if (isTaken) {
                    mMapManager.onSpotTaken(id);
                }
                updateFreeSpotCnt();
            }
        });
    }

    @Override
    public void onSegmentFreeCountChanged(final String id, final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSelectedSegment != null && mSelectedSegment.getId().equals(id)) {
                    mSelectedSegment.setFreeSpots(count);
                }
                if (mSegments.containsKey(id)) {
                    final Segment segment = mSegments.get(id);
                    segment.setFreeSpots(count);
                    mSegments.put(id, segment);
                    updateSegmentFreeCnt(segment);
                }
                mMapManager.onSegmentChanged(id, count);
                updateFreeSpotCnt();
            }
        });
    }

    @Override
    public void onSpotTimesChanged(final Map<String, Integer> times) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (String id : times.keySet()) {
                    if (mSpots.containsKey(id)) {
                        Spot spot = mSpots.get(id);
                        spot.setParkingTime(times.get(id));
                        mSpots.put(id, spot);
                    }
                    if (times.get(id) == 0) {
                        mMapManager.onSpotUnavailable(id);
                    } else if (times.get(id) < minTime) {
                        mMapManager.onSpotNoLongerSatisfyFilter(id);
                    }
                }
                if (mapMode == MapModes.SPOT_MARKERS) {
                    updateSpotMarkers(false);
                }
            }
        });
    }

    @Override
    public void onSegmentTimesChanged(final Map<String, Integer> times) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (String id : times.keySet()) {
                    if (mSegments.containsKey(id)) {
                        Segment segment = mSegments.get(id);
                        segment.setParkingTime(times.get(id));
                        mSegments.put(id, segment);
                    }
                    if (times.get(id) == 0) {
                        mMapManager.onSegmentUnavailable(id);
                    } else if (times.get(id) < minTime) {
                        mMapManager.onSegmentNoLongerSatisfyFilter(id);
                    }
                }
                if (mapMode == MapModes.SEGMENTS || mapMode == MapModes.SEGMENT_MARKERS) {
                    updateSegmentPolylines(false);
                    if (mapMode == MapModes.SEGMENT_MARKERS) {
                        updateSegmentMarkers();
                    }
                }
            }
        });
    }

    /* END UPDATE INFO CALLBACKS */
    /* MAP CALLBACKS */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap();
        mMapManager.onMapReady(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        initBitmapDescriptors();
    }

    @Override
    public void onCameraIdle() {
        mMapManager.onZoomChanged(getZoom());
        if (mapMode == MapModes.SPOT_MARKERS) {
            if (shouldClearMap) {
                updateSegmentPolylines(true);
            } else {
                updateSpotMarkers(true);
            }
            getSpots();
        } else if (mapMode == MapModes.SEGMENT_MARKERS || mapMode == MapModes.SEGMENTS) {
            if (shouldClearMap) {
                updateSpotMarkers(true);
            } else {
                updateSegmentPolylines(true);
                if (mapMode == MapModes.SEGMENT_MARKERS) {
                    updateSegmentMarkers();
                }
            }
            getSegments();
        }
        mMapManager.onCameraStoppedMoving(mMap.getCameraPosition().target);
    }

    @Override
    public void onCameraMove() {
        mMapManager.onMyLocationCentered(false);
        mMapManager.onBearingChanged(getBearing());
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mapMode == MapModes.SEGMENT_MARKERS || mapMode == MapModes.SEGMENTS) {
            Segment segment = mSegments.get(marker.getTag());
            if (segment != null) {
                mMapManager.onSegmentSelected(segment);
            }
        } else if (mapMode == MapModes.SPOT_MARKERS) {
            Spot spot = mSpots.get(marker.getTag());
            if (spot != null) {
                mMapManager.onSpotSelected(spot);
            }
        } else {
            mMapManager.onMarkerClick(marker);
        }
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMapManager.onMapClick(latLng);
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Segment segment = mSegments.get(polyline.getTag());
        if (segment != null) {
            mMapManager.onSegmentSelected(segment);
        }
    }

    /* END MAP CALLBACKS */
    /* MAP HOLDER */

    @Override
    public void requestMap(MapManager mapManager) {
        mMapManager = mapManager;
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.content_map);
        if (supportMapFragment == null) {
            supportMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.content_map, supportMapFragment).commit();
            supportMapFragment.getMapAsync(this);
        } else if (!isMapReady()) {
            supportMapFragment.getMapAsync(this);
        } else {
            mapManager.onMapReady(this);
        }
    }

    @Override
    public boolean isMapReady() {
        return mMap != null;
    }

    @Override
    public void resetPrefs() {
        if (isMapReady()) {
            deselectSpot();
            deselectSegment();
            setMaxZoom(ZoomLevels.DEFAULT_MAX);
            setPadding(0);
            if (mCar != null) {
                mCar.remove();
                mCar = null;
            }
            if (mSearchMarker != null) {
                mSearchMarker.remove();
                mSearchMarker = null;
            }
            if (mTimeMarker != null) {
                mTimeMarker.remove();
                mTimeMarker = null;
            }
            if (mDestMarker != null) {
                mDestMarker.remove();
                mDestMarker = null;
            }
        }
        mLocationManager.setUpdateFreq(LocationManager.DEFAULT_UPDATE_FREQ);
    }

    @Override
    public int getMapMode() {
        return mapMode;
    }

    @Override
    public void setMapMode(int mode) {
        if (mapMode == mode) return;
        int prevMode = mapMode;
        mapMode = mode;
        if (mapMode == MapModes.ROUTE) {
            mSpots.clear();
            subscribeSpots(new HashSet<>(mSpots.keySet()));
            mSegments.clear();
            subscribeSegments(new HashSet<>(mSegments.keySet()));
            clearMap();
        } else if (mapMode == MapModes.SPOT_MARKERS) {
            if (prevMode == MapModes.ROUTE) {
                mMap.clear();
            } else {
                shouldClearMap = true;
            }
            getSpots();
        } else if (mapMode == MapModes.SEGMENTS) {
            if (prevMode != MapModes.SEGMENT_MARKERS) {
                if (prevMode == MapModes.ROUTE) {
                    mMap.clear();
                } else {
                    shouldClearMap = true;
                }
                getSegments();
            } else {
                removeMarkers();
            }
        } else if (mapMode == MapModes.SEGMENT_MARKERS) {
            if (prevMode != MapModes.SEGMENTS) {
                shouldClearMap = true;
                getSegments();
            } else {
                showSegmentMarkers();
            }
        }
    }

    @Override
    public float getBearing() {
        return mMap.getCameraPosition().bearing;
    }

    @Override
    public void setBearing(float bearing) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mMap.getCameraPosition().target).zoom(getZoom()).bearing(bearing).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), COMPASS_ANIM_TIME, null);
    }

    @Override
    public LatLngBounds getBounds() {
        return mMap.getProjection().getVisibleRegion().latLngBounds;
    }

    @Override
    public LatLng getCenter() {
        return mMap.getCameraPosition().target;
    }

    @Override
    public void setMinZoom(float zoom) {
        mMap.setMinZoomPreference(zoom);
    }

    @Override
    public void setMaxZoom(float zoom) {
        mMap.setMaxZoomPreference(zoom);
    }

    @Override
    public float getZoom() {
        return mMap.getCameraPosition().zoom;
    }

    @Override
    public void setZoom(float zoom) {
        setZoom(zoom, mMap.getCameraPosition().target);
    }

    @Override
    public void setZoom(float zoom, LatLng target) {
        setZoom(zoom, target, null);
    }

    @Override
    public void setZoom(float zoom, GoogleMap.CancelableCallback cancelableCallback) {
        setZoom(zoom, mMap.getCameraPosition().target, cancelableCallback);
    }

    @Override
    public void setZoom(float zoom, LatLng target, GoogleMap.CancelableCallback cancelableCallback) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(target).zoom(zoom).bearing(mMap.getCameraPosition().bearing).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), DEFAULT_ANIM_TIME, cancelableCallback);
    }

    @Override
    public void showRoute(List<com.google.maps.model.LatLng> route) {
        List<LatLng> routePoints = new LinkedList<>();
        for (com.google.maps.model.LatLng point : route) {
            routePoints.add(new LatLng(point.lat, point.lng));
        }
        Polyline prevRoute = mPolylines.get(BundleArguments.ROUTE);
        if (prevRoute == null) {
            PolylineOptions polylineOptions = new PolylineOptions().width(10).color(colorBlue).addAll(routePoints);
            mPolylines.put(BundleArguments.ROUTE, mMap.addPolyline(polylineOptions));
        } else {
            prevRoute.setPoints(routePoints);
        }
    }

    @Override
    public void showDestLoc(LatLng latLng) {
        if (mDestMarker == null) {
            mDestMarker = mMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f).icon(mDestinationBD).flat(true));
        } else {
            mDestMarker.setPosition(latLng);
        }
    }

    @Override
    public void showCar(LatLng latLng) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(BitmapUtils.rotate(mCarBitmap, mLocationManager.getLastKnownBearing()));
        if (mCar == null) {
            mCar = mMap.addMarker(new MarkerOptions().position(latLng).icon(icon).anchor(0.5f, 0.5f).flat(true));
        } else {
            mCar.setPosition(latLng);
            mCar.setIcon(icon);
        }
    }

    @Override
    public void selectSpot(Spot spot) {
        mSelectedSpot = spot;
        Marker marker = mMarkers.get(spot.getId());
        if (marker != null) {
            marker.setIcon(mSelectedBD[spot.getType()]);
        }
    }

    @Override
    public void deselectSpot() {
        if (mSelectedSpot == null) return;
        Marker marker = mMarkers.get(mSelectedSpot.getId());
        if (marker != null) {
            if (showOnlyOneMarker) {
                marker.remove();
            } else {
                marker.setIcon(mFreeBD[mSelectedSpot.getType()]);
            }
        }
        mSelectedSpot = null;
    }

    @Override
    public void selectSegment(Segment segment) {
        deselectSegment();
        mSelectedSegment = segment;
        if (!mSegments.containsKey(segment.getId())) return;
        drawSelectedSegmentPolyline();
        drawSelectedSegmentMarker();
    }

    @Override
    public void deselectSegment() {
        if (mSelectedSegment == null) return;
        if (mSelectedSegment.getTotalSpots() > 1) {
            if (mSelectedSegmentBorderPolyline != null) {
                mSelectedSegmentBorderPolyline.remove();
                mSelectedSegmentBorderPolyline = null;
            }
        } else {
            Marker circleMarker = mCircleMarkers.get(mSelectedSegment.getId());
            if (circleMarker != null) {
                Segment segment = mSegments.get(mSelectedSegment.getId());
                if (segment != null) {
                    circleMarker.setIcon(segment.getFreeSpots() > 0 ? mFreeCircleBD : mTakenCircleBD);
                } else {
                    circleMarker.remove();
                    mCircleMarkers.remove(mSelectedSegment.getId());
                }
            }
        }
        Marker segmentMarker = mMarkers.get(mSelectedSegment.getId());
        if (segmentMarker != null) {
            if (showOnlyOneMarker) {
                segmentMarker.remove();
                mMarkers.remove(mSelectedSegment.getId());
            } else {
                segmentMarker.setIcon(getMarkerOptions(mSelectedSegment, false).getIcon());
            }
        }
        mSelectedSegment = null;
    }

    @Override
    public void updateFreeSpotCnt() {
        if (shouldClearMap) return;
        int freeCnt = 0;
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        if (mapMode == MapModes.SPOT_MARKERS) {
            for (String key : mSpots.keySet()) {
                Spot spot = mSpots.get(key);
                if (spot.isFree() && spot.getParkingTime() >= minTime && bounds.contains(spot.getLatLng()) && mMarkers.containsKey(spot.getId())) {
                    freeCnt++;
                }
            }
        } else {
            for (String key : mSegments.keySet()) {
                Segment segment = mSegments.get(key);
                if (bounds.contains(segment.getCentralSpot()) && (mPolylines.containsKey(segment.getId())) || mCircleMarkers.containsKey(segment.getId())) {
                    freeCnt += segment.getFreeSpots();
                }
            }
        }
        mMapManager.onFreeSpotCntChanged(freeCnt);
    }

    @Override
    public void requestRoadLocation() {
        mLocationManager.requestRoadLocation();
    }

    @Override
    public void requestGPSLocation() {
        mLocationManager.requestGPSLocation();
    }

    @Override
    public void setLocationUpdateFreq(long updateFreq) {
        mLocationManager.setUpdateFreq(updateFreq);
    }

    @Override
    public void setShowOnlyOneMarker(boolean show) {
        showOnlyOneMarker = show;
    }

    @Override
    public void showParkingTimeMarker(final Spot parkedSpot) {
        int timeLeftSec = parkedSpot.getTimeRemainingSec();
        int minutes = (timeLeftSec / 60) % 60;
        int hours = (timeLeftSec / 3600);
        final View markerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_parking_time, null);
        final TextView txtTimeLeft = (TextView) markerView.findViewById(R.id.tv_time_remaining);
        txtTimeLeft.setText(String.format(Locale.US, "%02d:%02d", hours, minutes));
        Bitmap bitmap = BitmapUtils.fromView(markerView);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        if (mTimeMarker != null) {
            mTimeMarker.setIcon(bitmapDescriptor);
        } else {
            mTimeMarker = mMap.addMarker(new MarkerOptions().position(parkedSpot.getLatLng()).anchor(0.5f, 0.5f).icon(bitmapDescriptor));
        }
        mCountDownTimer = new CountDownTimer(timeLeftSec * 1000L, TIMER_STEP) {

            @Override
            public void onTick(long millisUntilFinished) {
                int hours = (int) millisUntilFinished / 1000 / 60 / 60;
                millisUntilFinished -= hours * 1000 * 60 * 60;
                int minutes = (int) millisUntilFinished / 1000 / 60;
                millisUntilFinished -= minutes * 1000 * 60;
                int seconds = (int) millisUntilFinished / 1000;
                if (seconds == 59) {
                    txtTimeLeft.setText(String.format(Locale.US, "%02d:%02d", hours, minutes));
                    Bitmap bitmap = BitmapUtils.fromView(markerView);
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                    if (mTimeMarker != null) {
                        mTimeMarker.setIcon(bitmapDescriptor);
                    } else {
                        mTimeMarker = mMap.addMarker(new MarkerOptions().position(parkedSpot.getLatLng()).anchor(0.5f, 0.5f).icon(bitmapDescriptor));
                    }
                }
            }

            @Override
            public void onFinish() {
                txtTimeLeft.setText(R.string.time_00_00);
                Bitmap bitmap = BitmapUtils.fromView(markerView);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                if (mTimeMarker != null) {
                    mTimeMarker.setIcon(bitmapDescriptor);
                } else {
                    mTimeMarker = mMap.addMarker(new MarkerOptions().position(parkedSpot.getLatLng()).anchor(0.5f, 0.5f).icon(bitmapDescriptor));
                }
            }
        }.start();
    }

    @Override
    public void addSearchMarker(LatLng latLng) {
        if (mSearchMarker == null) {
            mSearchMarker = mMap.addMarker(new MarkerOptions().position(latLng).anchor(0.5f, 0.5f));
        } else {
            mSearchMarker.setPosition(latLng);
        }
    }

    @Override
    public void removeSearchMarker() {
        if (mSearchMarker != null) {
            mSearchMarker.remove();
            mSearchMarker = null;
        }
    }

    @Override
    public void setPadding(int bottomPadding) {
        mMap.setPadding(0, 0, 0, bottomPadding);
    }

    @Override
    public void setMinTime(int time) {
        minTime = time;
        if (mapMode == MapModes.SPOT_MARKERS) {
            updateSpotMarkers(false);
        } else if (mapMode == MapModes.SEGMENT_MARKERS || mapMode == MapModes.SEGMENTS) {
            updateSegmentPolylines(false);
            if (mapMode == MapModes.SEGMENT_MARKERS) {
                updateSegmentMarkers();
            }
        }
    }

    @Override
    public int getMinTime() {
        return minTime;
    }

    @Override
    public void adjustZoomForRoute() {
        Polyline polyline = mPolylines.get(BundleArguments.ROUTE);
        if (polyline == null) return;
        List<LatLng> route = polyline.getPoints();
        LatLngBounds bounds = new LatLngBounds.Builder().include(route.get(0)).include(route.get(route.size() - 1)).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Converter.dpToPx(50)));
    }

    @Override
    public void clearMap() {
        for (String key : mMarkers.keySet()) {
            mMarkers.get(key).remove();
        }
        for (String key : mPolylines.keySet()) {
            mPolylines.get(key).remove();
        }
        for (String key : mCircleMarkers.keySet()) {
            mCircleMarkers.get(key).remove();
        }
        if (mSelectedSegmentBorderPolyline != null) {
            mSelectedSegmentBorderPolyline.remove();
            mSelectedSegmentBorderPolyline = null;
        }
        mMarkers.clear();
        mPolylines.clear();
        mCircleMarkers.clear();
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public Location getCurrentRoadLocation() {
        return mLocationManager.getLastKnownRoadLocation();
    }

    @Override
    public Location getCurrentLocation() {
        return mLocationManager.getLastKnownLocation();
    }

    @Override
    public void moveToLocation(final LatLng latLng) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                showCar(latLng);
                mMapManager.onMyLocationCentered(true);
            }

            @Override
            public void onCancel() {
                mMapManager.onMyLocationCentered(false);
            }
        });
    }

    @Override
    public void subscribeSpots(Set<String> spotIds) {
        if (mSelectedSpot != null) {
            spotIds.add(mSelectedSpot.getId());
        }
        mInfoUpdateManager.subscribeSpots(spotIds);
    }

    @Override
    public void subscribeSegments(Set<String> segmentIds) {
        if (mSelectedSegment != null) {
            segmentIds.add(mSelectedSegment.getId());
        }
        mInfoUpdateManager.subscribeSegments(segmentIds);
    }

    @Override
    public void takeMapScreenshot(GoogleMap.SnapshotReadyCallback callback) {
        mMap.snapshot(callback);
    }

    @Override
    public void setMyLocationButtonEnabled() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
        if (mLocationManager.getLastKnownLocation() != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(mLocationManager.getLastKnownLocation().getLatLng());
            mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mMapManager.onMyLocationCentered(true);
                }

                @Override
                public void onCancel() {
                    mMapManager.onMyLocationCentered(false);
                }
            });
        }
    }

    /* END MAP HOLDER */
    /* MAP */

    private void showSpotMarkers() {
        if (shouldClearMap) {
            clearMap();
            mSegments.clear();
            subscribeSegments(new HashSet<String>());
            shouldClearMap = false;
        } else {
            for (String key : mMarkers.keySet()) {
                if (!mSpots.containsKey(key)) {
                    mMarkers.get(key).remove();
                    mMarkers.remove(key);
                }
            }
        }
        updateSpotMarkers(true);
    }

    private void updateSpotMarkers(boolean shouldUpdateSize) {
        boolean sizeUpdated = shouldUpdateSize && updateSpotMarkerSize();
        for (String key : mSpots.keySet()) {
            Spot spot = mSpots.get(key);
            Marker marker = mMarkers.get(spot.getId());
            if (marker == null) {
                if (spot.getParkingTime() >= minTime) {
                    addSpot(spot);
                }
            } else {
                if (spot.getParkingTime() < minTime) {
                    marker.remove();
                    mMarkers.remove(spot.getId());
                } else if (sizeUpdated) {
                    marker.setIcon(getSpotBitmapDescriptor(spot));
                }
            }
        }
        updateFreeSpotCnt();
    }

    private void showSegments() {
        if (shouldClearMap) {
            clearMap();
            mSpots.clear();
            subscribeSpots(new HashSet<String>());
            shouldClearMap = false;
        } else {
            for (String key : mPolylines.keySet()) {
                if (!mSegments.containsKey(key)) {
                    mPolylines.get(key).remove();
                    mPolylines.remove(key);
                    if (mSelectedSegment != null && mSelectedSegment.getId().equals(key)) {
                        mSelectedSegmentBorderPolyline.remove();
                        mSelectedSegmentBorderPolyline = null;
                    }
                }
            }
            for (String key : mCircleMarkers.keySet()) {
                if (!mSegments.containsKey(key)) {
                    mCircleMarkers.get(key).remove();
                    mCircleMarkers.remove(key);
                }
            }
        }
        updateSegmentPolylines(true);
    }

    private void showSegmentMarkers() {
        for (String key : mMarkers.keySet()) {
            if (!mSegments.containsKey(key)) {
                mMarkers.get(key).remove();
                mMarkers.remove(key);
            }
        }
        updateSegmentMarkers();
    }

    private void updateSegmentPolylines(boolean shouldUpdateSize) {
        boolean sizeUpdated = shouldUpdateSize && updateCircleMakerSize();
        for (String key : mSegments.keySet()) {
            Segment segment = mSegments.get(key);
            Polyline polyline = mPolylines.get(key);
            Marker circleMarker = mCircleMarkers.get(key);
            if (polyline != null) {
                if (segment.getParkingTime() < minTime) {
                    polyline.remove();
                    mPolylines.remove(key);
                    if (mSelectedSegment != null && mSelectedSegment.equals(segment)) {
                        mSelectedSegmentBorderPolyline.remove();
                        mSelectedSegmentBorderPolyline = null;
                    }
                } else if (sizeUpdated) {
                    if (mSelectedSegment != null && mSelectedSegment.equals(segment)) {
                        mSelectedSegmentBorderPolyline.setWidth(circleMarkerSizePx + Converter.dpToPx(4));
                    }
                    polyline.setWidth(circleMarkerSizePx);
                }
            } else if (circleMarker != null) {
                if (segment.getParkingTime() < minTime) {
                    circleMarker.remove();
                    mCircleMarkers.remove(key);
                } else if (sizeUpdated) {
                    if (mSelectedSegment != null && mSelectedSegment.equals(segment)) {
                        circleMarker.setIcon(mSelectedCircleBD);
                    } else {
                        circleMarker.setIcon(segment.getFreeSpots() == 0 ? mTakenCircleBD : mFreeCircleBD);
                    }
                }
            } else {
                if (segment.getParkingTime() >= minTime) {
                    if (mSelectedSegment != null && mSelectedSegment.equals(segment)) {
                        drawSelectedSegmentPolyline();
                    } else {
                        addSegment(segment);
                    }
                }
            }
        }
        updateFreeSpotCnt();
    }

    private void updateSegmentMarkers() {
        if (showOnlyOneMarker) {
            if (mSelectedSegment != null) {
                Marker selectedSegmentMarker = mMarkers.get(mSelectedSegment.getId());
                if (mSegments.containsKey(mSelectedSegment.getId())) {
                    if (selectedSegmentMarker == null) {
                        drawSelectedSegmentMarker();
                    }
                } else {
                    if (selectedSegmentMarker != null) {
                        selectedSegmentMarker.remove();
                        mMarkers.remove(mSelectedSegment.getId());
                    }
                }
            }
        } else {
            for (String key : mSegments.keySet()) {
                Segment segment = mSegments.get(key);
                Marker segmentMarker = mMarkers.get(segment.getId());
                if (segmentMarker != null) {
                    if (segment.getParkingTime() < minTime) {
                        segmentMarker.remove();
                        mMarkers.remove(segment.getId());
                    }
                } else {
                    if (segment.getParkingTime() >= minTime) {
                        if (mSelectedSegment != null && mSelectedSegment.equals(segment)) {
                            drawSelectedSegmentMarker();
                        } else {
                            addSegmentMarker(segment);
                        }
                    }
                }
            }
        }
    }

    private void updateSpotState(Spot spot) {
        Marker marker = mMarkers.get(spot.getId());
        if (marker != null) {
            marker.setIcon(getSpotBitmapDescriptor(spot));
        }
    }

    private void updateSegmentFreeCnt(Segment segment) {
        Polyline polyline = mPolylines.get(segment.getId());
        Marker circleMarker = mCircleMarkers.get(segment.getId());
        Marker segmentMarker = mMarkers.get(segment.getId());
        if (polyline != null) {
            polyline.setColor(getSegmentColor(segment));
        } else if (circleMarker != null) {
            circleMarker.setIcon(segment.getFreeSpots() == 0 ? mTakenCircleBD : mFreeCircleBD);
        }
        if (mapMode == MapModes.SEGMENT_MARKERS) {
            if (segmentMarker != null) {
                if (segment.getFreeSpots() > 0) {
                    segmentMarker.setIcon(getMarkerOptions(segment, mSelectedSegment != null && mSelectedSegment.equals(segment)).getIcon());
                } else {
                    segmentMarker.remove();
                    mMarkers.remove(segment.getId());
                }
            } else {
                if (mSegments.containsKey(segment.getId()) && !showOnlyOneMarker) {
                    addSegmentMarker(segment);
                }
            }
        }
    }

    private void drawSelectedSegmentPolyline() {
        if (mSelectedSegment == null) return;
        if (mSelectedSegment.getTotalSpots() > 1) {
            PolylineOptions borderPolylineOptions = new PolylineOptions().width(circleMarkerSizePx + Converter.dpToPx(4)).color(Color.WHITE)
                    .startCap(new RoundCap()).endCap(new RoundCap());
            PolylineOptions polylineOptions = new PolylineOptions().width(circleMarkerSizePx).color(getSegmentColor(mSelectedSegment))
                    .startCap(new RoundCap()).endCap(new RoundCap());
            for (LatLng latLng : mSelectedSegment.getGeometry()) {
                polylineOptions.add(latLng);
                borderPolylineOptions.add(latLng);
            }
            mSelectedSegmentBorderPolyline = mMap.addPolyline(borderPolylineOptions);
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setClickable(true);
            polyline.setTag(mSelectedSegment.getId());
            Polyline prevPolyline = mPolylines.get(mSelectedSegment.getId());
            if (prevPolyline != null) {
                prevPolyline.remove();
                mPolylines.remove(mSelectedSegment.getId());
            }
            mPolylines.put(mSelectedSegment.getId(), polyline);
        } else {
            Marker marker = mCircleMarkers.get(mSelectedSegment.getId());
            if (marker != null) {
                marker.setIcon(mSelectedCircleBD);
            } else {
                addSegment(mSelectedSegment);
                drawSelectedSegmentPolyline();
            }
        }
    }

    private void drawSelectedSegmentMarker() {
        if (mSelectedSegment == null) return;
        if (showOnlyOneMarker) {
            removeMarkers();
            mMarkers.put(mSelectedSegment.getId(), mMap.addMarker(getMarkerOptions(mSelectedSegment, true)));
        } else {
            Marker marker = mMarkers.get(mSelectedSegment.getId());
            if (marker != null) {
                marker.setIcon(getMarkerOptions(mSelectedSegment, true).getIcon());
            } else {
                addSegmentMarker(mSelectedSegment);
            }
        }
    }

    private void removeMarkers() {
        for (String key : mMarkers.keySet()) {
            mMarkers.get(key).remove();
        }
        mMarkers.clear();
    }

    private boolean updateSpotMarkerSize() {
        int size = getSpotMarkerSizePx();
        if (Math.abs(size - spotMarkerSizePx) < 10) return false;
        for (int i = 0; i < 3; i++) {
            mFreeBD[i] = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(mFreeBitmap[i], size, size));
            mTakenBD[i] = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(mTakenBitmap[i], size, size));
            mSelectedBD[i] = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(mSelectedBitmap[i], size, size));
        }
        spotMarkerSizePx = size;
        return true;
    }

    private boolean updateCircleMakerSize() {
        int size = (int) getPolylineWidth();
        if (Math.abs(size - circleMarkerSizePx) < 10) return false;
        mFreeCircleBD = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(mFreeCircleBitmap, size, size));
        mTakenCircleBD = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(mTakenCircleBitmap, size, size));
        mSelectedCircleBD = BitmapDescriptorFactory.fromBitmap(BitmapUtils.resize(mSelectedCircleBitmap, size + Converter.dpToPx(2), size + Converter.dpToPx(2)));
        circleMarkerSizePx = size;
        return true;
    }

    private void addSpot(Spot spot) {
        MarkerOptions markerOptions = getMarkerOptions(spot);
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(spot.getId());
        mMarkers.put(spot.getId(), marker);
    }

    private void addSegment(Segment segment) {
        if (segment.getTotalSpots() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions().width(circleMarkerSizePx).color(getSegmentColor(segment))
                    .startCap(new RoundCap()).endCap(new RoundCap());
            for (LatLng latLng : segment.getGeometry()) {
                polylineOptions.add(latLng);
            }
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setClickable(true);
            polyline.setTag(segment.getId());
            mPolylines.put(segment.getId(), polyline);
        } else {
            MarkerOptions markerOptions = new MarkerOptions().position(segment.getCentralSpot()).anchor(0.5f, 0.5f).flat(true);
            if (segment.getFreeSpots() == 1) {
                markerOptions.icon(mFreeCircleBD);
            } else {
                markerOptions.icon(mTakenCircleBD);
            }
            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(segment.getId());
            mCircleMarkers.put(segment.getId(), marker);
        }
    }

    private void addSegmentMarker(Segment segment) {
        if (segment.getFreeSpots() == 0) return;
        Marker marker = mMap.addMarker(getMarkerOptions(segment, mSelectedSegment != null && mSelectedSegment.getId().equals(segment.getId())));
        marker.setTag(segment.getId());
        mMarkers.put(segment.getId(), marker);
    }

    private int getSpotMarkerSizePx() {
        return (int) (25.0 / getMetersPerPixel());
    }

    private float getPolylineWidth() {
        return (float) (20.0 / getMetersPerPixel());
    }

    private MarkerOptions getMarkerOptions(Spot spot) {
        MarkerOptions markerOptions = new MarkerOptions().position(spot.getLatLng()).anchor(0.5f, 0.5f).flat(true);
        markerOptions.icon(getSpotBitmapDescriptor(spot));
        return markerOptions;
    }

    private BitmapDescriptor getSpotBitmapDescriptor(Spot spot) {
        int type = spot.getType();
        return (mSelectedSpot != null && mSelectedSpot.equals(spot)) ? mSelectedBD[type] : spot.isFree() ? mFreeBD[type] : mTakenBD[type];
    }

    private double getMetersPerPixel() {
        return 156543.03392 * Math.cos(mMap.getCameraPosition().target.latitude * Math.PI / 180.0) / Math.pow(2, getZoom());
    }

    private MarkerOptions getMarkerOptions(Segment segment, boolean selected) {
        int markerLayout = selected ? R.layout.marker_segment_selected : R.layout.marker_segment;
        View lineMarkerView = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(markerLayout, null);
        ((TextView) lineMarkerView.findViewById(R.id.tv_segment_marker_count)).setText(String.valueOf(segment.getFreeSpots()));
        LatLng latLng = segment.getCentralSpot();
        Bitmap bitmap = BitmapUtils.fromView(lineMarkerView);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        return new MarkerOptions().position(latLng).icon(bitmapDescriptor);
    }

    private int getSegmentColor(Segment segment) {
        int freeSpotsCount = segment.getFreeSpots();
        if (freeSpotsCount > 4) return colorSegmentGreen;
        else if (freeSpotsCount > 0) return colorSegmentYellow;
        else return colorSegmentRed;
    }

    public static double getDistance(com.google.android.gms.maps.model.LatLng
                                             a, com.google.android.gms.maps.model.LatLng b) {
        double lat1 = a.latitude * Math.PI / 180;
        double lat2 = b.latitude * Math.PI / 180;
        double lng1 = a.longitude * Math.PI / 180;
        double lng2 = b.longitude * Math.PI / 180;
        double cl1 = Math.cos(lat1);
        double cl2 = Math.cos(lat2);
        double sl1 = Math.sin(lat1);
        double sl2 = Math.sin(lat2);
        double delta = lng2 - lng1;
        double cdelta = Math.cos(delta);
        double sdelta = Math.sin(delta);
        double y = Math.sqrt(Math.pow(cl2 * sdelta, 2) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
        double x = sl1 * sl2 + cl1 * cl2 * cdelta;
        double ad = Math.atan2(y, x);
        return ad * EARTH_RADIUS;
    }

    /* END MAP */
    /* LOCATION */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationManager.startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationPermitted() {
        mMapManager.onLocationPermitted();
    }

    @Override
    public void onLocationUnavailable() {
        mMapManager.onLocationUnavailable();
    }

    @Override
    public void onLocationGranted(Location location) {
        mMapManager.onLocationGranted(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMapManager.onLocationChanged(location);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMapManager.onMyLocationCentered(false);
            }
        });
    }

    /* END LOCATION */
    /* QUERIES */

    private void getSpots() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng center = bounds.getCenter();
        double radius = getDistance(bounds.northeast, bounds.southwest);
        mParkUrbnApi.getSpots(new MapRequest(new com.cruxlab.parkurbn.model.Location(center), radius)).enqueue(new ResponseCallback<List<Spot>>() {
            @Override
            public void handleResponse(Response<List<Spot>> response) {
                if (mapMode != MapModes.SPOT_MARKERS) return;
                mSpots.clear();
                ArrayList<Spot> spots = (ArrayList<Spot>) response.body();
                for (Spot spot : spots) {
                    mSpots.put(spot.getId(), spot);
                }
                subscribeSpots(new HashSet<>(mSpots.keySet()));
                if (isMapReady()) {
                    showSpotMarkers();
                }
            }
        });
    }

    private void getSegments() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng center = bounds.getCenter();
        double radius = getDistance(bounds.northeast, bounds.southwest);
        mParkUrbnApi.getSegments(new MapRequest(new com.cruxlab.parkurbn.model.Location(center), radius)).enqueue(new ResponseCallback<List<Segment>>() {
            @Override
            public void onFailure(Call<List<Segment>> call, Throwable t) {
                super.onFailure(call, t);
                hideLoader();
            }

            @Override
            protected void showErrorMessage(ErrorResponse response) {
                hideLoader();
            }

            @Override
            public void handleResponse(Response<List<Segment>> response) {
                hideLoader();
                if (mapMode != MapModes.SEGMENTS && mapMode != MapModes.SEGMENT_MARKERS) return;
                Collection<Segment> segments = response.body();
                mSegments.clear();
                for (Segment segment : segments) {
                    mSegments.put(segment.getId(), segment);
                }
                subscribeSegments(new HashSet<>(mSegments.keySet()));
                if (isMapReady()) {
                    showSegments();
                    if (mapMode == MapModes.SEGMENT_MARKERS) {
                        showSegmentMarkers();
                    }
                }
            }
        });
    }

    /* END QUERIES */

    public void setOverlayFragment(Fragment overlayFragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getSupportFragmentManager().findFragmentById(R.id.content_overlay) == null) {
            transaction.add(R.id.content_overlay, overlayFragment);
        } else {
            transaction.replace(R.id.content_overlay, overlayFragment);
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public void clearFragmentsBackStaсk() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void openDrawer() {
        drawer.openDrawer(GravityCompat.START);
    }

    private void setupDrawer() {
        lvMenu.setAdapter(new MenuAdapter(MapActivity.this));
        lvMenu.setOnItemClickListener(this);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("v" + info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void logout() {
        showLoader();
        Call<JSONObject> call = mParkUrbnApi.logout();
        call.enqueue(new ResponseCallback<JSONObject>() {
            @Override
            protected void showErrorMessage(ErrorResponse response) {
                super.showErrorMessage(response);
                Toast.makeText(MapActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                hideLoader();
            }

            @Override
            public void handleResponse(Response<JSONObject> response) {
                hideLoader();
                SharedPrefsManager.get().clearUser();
                LoginManager.getInstance().logOut();

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        ParkUrbnApplication.get().getDb().historyDao().deleteAll();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        startActivity(new Intent(MapActivity.this, StartActivity.class));
                        finish();
                    }
                }.execute();
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                super.onFailure(call, t);
                hideLoader();
                tvSignOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signOut();
                    }
                });
            }
        });
    }

    private void setLockDrawer(boolean isLocked) {
        int lockMode = isLocked ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawer.setDrawerLockMode(lockMode);
        if (getSupportActionBar() != null) {
            toggle.setDrawerIndicatorEnabled(!isLocked);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(!isLocked);
        }
    }

    public void superOnBackPressed() {
        super.onBackPressed();
    }

}