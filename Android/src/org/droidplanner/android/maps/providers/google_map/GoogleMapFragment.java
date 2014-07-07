package org.droidplanner.android.maps.providers.google_map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.helpers.LocalMapTileProvider;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.common.collect.HashBiMap;

public class GoogleMapFragment extends SupportMapFragment implements DPMap,
GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = GoogleMapFragment.class.getSimpleName();

    public static final String PREF_MAP_TYPE = "pref_map_type";

    public static final String MAP_TYPE_SATELLITE = "Satellite";
    public static final String MAP_TYPE_HYBRID = "Hybrid";
    public static final String MAP_TYPE_NORMAL = "Normal";
    public static final String MAP_TYPE_TERRAIN = "Terrain";

    //TODO: update the interval based on the user's current activity.
    private static final long USER_LOCATION_UPDATE_INTERVAL = 5000; //ms
    private static final long USER_LOCATION_UPDATE_FASTEST_INTERVAL = 1000; //ms
    private static final float USER_LOCATION_UPDATE_MIN_DISPLACEMENT = 5; //m

    private final HashBiMap<MarkerInfo, Marker> mMarkers = HashBiMap.create();

    private Drone mDrone;
    private DroidPlannerPrefs mAppPrefs;

    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>
            (AutoPanMode.DISABLED);
    private final LinkedList<Runnable> mApiClientTasks = new LinkedList<Runnable>();

    private final Runnable mGoToMyLocationTask = new Runnable() {
        @Override
        public void run() {
            if(mApiClient.isConnected()){
                final Location myLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mApiClient);
                if(myLocation != null){
                    final float currentZoomLevel = mMap.getCameraPosition().zoom;
                    updateCamera(DroneHelper.LocationToCoord(myLocation), (int)currentZoomLevel);
                }
            }
            else{
                mApiClientTasks.add(this);
            }
        }
    };

    private final Runnable mRemoveLocationUpdateTask = new Runnable() {
        @Override
        public void run() {
            if(mApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates
                        (mApiClient, GoogleMapFragment.this);
            }
            else{
                mApiClientTasks.add(this);
            }
        }
    };

    private final Runnable mRequestLocationUpdateTask = new Runnable() {
        @Override
        public void run() {
            if(mApiClient.isConnected()){
                final LocationRequest locationReq = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setFastestInterval(USER_LOCATION_UPDATE_FASTEST_INTERVAL)
                        .setInterval(USER_LOCATION_UPDATE_INTERVAL)
                        .setSmallestDisplacement(USER_LOCATION_UPDATE_MIN_DISPLACEMENT);
                LocationServices.FusedLocationApi.requestLocationUpdates
                        (mApiClient, locationReq, GoogleMapFragment.this);
            }
            else{
                mApiClientTasks.add(this);
            }
        }
    };

    private GoogleMap mMap;
    private GoogleApiClient mApiClient;

    private Polyline flightPath;
    private Polyline missionPath;
    private Polyline mDroneLeashPath;
    private int maxFlightPathSize;

    /*
    DP Map listeners
     */
    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        final FragmentActivity activity = getActivity();
        final Context context = activity.getApplicationContext();

        final View view = super.onCreateView(inflater, viewGroup, bundle);
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .build();

        mDrone = ((DroidPlannerApp)activity.getApplication()).getDrone();
        mAppPrefs = new DroidPlannerPrefs(context);

        final Bundle args = getArguments();
        if(args != null){
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        mApiClient.registerConnectionCallbacks(this);
        mApiClient.connect();
        setupMap();
    }

    @Override
    public void onStop(){
        super.onStop();
        mApiClient.unregisterConnectionCallbacks(this);
        mApiClient.disconnect();
        mApiClientTasks.clear();
    }

    @Override
    public void clearFlightPath() {
        if(flightPath != null) {
            List<LatLng> oldFlightPath = flightPath.getPoints();
            oldFlightPath.clear();
            flightPath.setPoints(oldFlightPath);
        }
    }

    @Override
    public void selectAutoPanMode(AutoPanMode target) {
        final AutoPanMode currentMode = mPanMode.get();
        if(currentMode == target)
            return;

        setAutoPanMode(currentMode, target);
    }

    private void setAutoPanMode(AutoPanMode current, AutoPanMode update){
        if(mPanMode.compareAndSet(current, update)){
            switch(current){
                case DRONE:
                    mDrone.events.removeDroneListener(this);
                    break;

                case USER:
                    if(mApiClient.isConnected()){
                        mRemoveLocationUpdateTask.run();
                    }
                    else{
                        mApiClientTasks.add(mRemoveLocationUpdateTask);
                    }
                    break;

                case DISABLED:
                default:
                    break;
            }

            switch(update){
                case DRONE:
                    mDrone.events.addDroneListener(this);
                    break;

                case USER:
                    if(mApiClient.isConnected()){
                        mRequestLocationUpdateTask.run();
                    }
                    else{
                        mApiClientTasks.add(mRequestLocationUpdateTask);
                    }
                    break;

                case DISABLED:
                default:
                    break;
            }
        }
    }

    @Override
    public DPMapProvider getProvider(){
        return DPMapProvider.GOOGLE_MAP;
    }

    @Override
    public void addFlightPathPoint(Coord2D coord) {
        final LatLng position = DroneHelper.CoordToLatLang(coord);

        if (maxFlightPathSize > 0) {
            if(flightPath == null){
                PolylineOptions flightPathOptions = new PolylineOptions();
                flightPathOptions.color(FLIGHT_PATH_DEFAULT_COLOR).width(FLIGHT_PATH_DEFAULT_WIDTH).zIndex(1);
                flightPath = mMap.addPolyline(flightPathOptions);
            }


            List<LatLng> oldFlightPath = flightPath.getPoints();
            if (oldFlightPath.size() > maxFlightPathSize) {
                oldFlightPath.remove(0);
            }
            oldFlightPath.add(position);
            flightPath.setPoints(oldFlightPath);
        }
    }

    @Override
    public void cleanMarkers(){
        for(Map.Entry<MarkerInfo, Marker> entry: mMarkers.entrySet()){
            Marker marker = entry.getValue();
            marker.remove();
        }

        mMarkers.clear();
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo){
        updateMarker(markerInfo, markerInfo.isDraggable());
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable){
        final LatLng position = DroneHelper.CoordToLatLang(markerInfo.getPosition());
        Marker marker = mMarkers.get(markerInfo);
        if(marker == null){
            //Generate the marker
            marker = mMap.addMarker(new MarkerOptions().position(position));
            mMarkers.put(markerInfo, marker);
        }

        //Update the marker
        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if(markerIcon != null){
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        marker.setAlpha(markerInfo.getAlpha());
        marker.setAnchor(markerInfo.getAnchorU(), markerInfo.getAnchorV());
        marker.setInfoWindowAnchor(markerInfo.getInfoWindowAnchorU(), markerInfo.getInfoWindowAnchorV());
        marker.setPosition(position);
        marker.setRotation(markerInfo.getRotation());
        marker.setSnippet(markerInfo.getSnippet());
        marker.setTitle(markerInfo.getTitle());
        marker.setDraggable(isDraggable);
        marker.setFlat(markerInfo.isFlat());
        marker.setVisible(markerInfo.isVisible());
    }

    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos){
        for(MarkerInfo info: markersInfos){
            updateMarker(info);
        }
    }

    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos, boolean isDraggable){
        for(MarkerInfo info: markersInfos){
            updateMarker(info, isDraggable);
        }
    }

    /**
     * Used to retrieve the info for the given marker.
     * @param marker marker whose info to retrieve
     * @return marker's info
     */
    private MarkerInfo getMarkerInfo(Marker marker){
        return mMarkers.inverse().get(marker);
    }

    @Override
    public List<Coord2D> projectPathIntoMap(List<Coord2D> path){
        List<Coord2D> coords = new ArrayList<Coord2D>();
        Projection projection = mMap.getProjection();

        for (Coord2D point : path) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getX(), (int) point.getY()));
            coords.add(DroneHelper.LatLngToCoord(coord));
        }

        return coords;
    }

    @Override
    public void setMapPadding(int left, int top, int right, int bottom){
        mMap.setPadding(left, top, right, bottom);
    }

    @Override
    public void setOnMapClickListener(OnMapClickListener listener){
        mMapClickListener = listener;
    }

    @Override
    public void setOnMapLongClickListener(OnMapLongClickListener listener){
        mMapLongClickListener = listener;
    }

    @Override
    public void setOnMarkerDragListener(OnMarkerDragListener listener){
        mMarkerDragListener = listener;
    }

    @Override
    public void setOnMarkerClickListener(OnMarkerClickListener listener){
        mMarkerClickListener = listener;
    }

    @Override
    public void updateCamera(Coord2D coord, int zoomLevel){
        if(coord != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneHelper.CoordToLatLang(coord),
                    zoomLevel));
        }
    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource){
        List<Coord2D> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for(Coord2D coord: pathCoords){
            pathPoints.add(DroneHelper.CoordToLatLang(coord));
        }

        if(mDroneLeashPath == null){
            PolylineOptions flightPath = new PolylineOptions();
            flightPath.color(DRONE_LEASH_DEFAULT_COLOR).width(DroneHelper.scaleDpToPixels
                    (DRONE_LEASH_DEFAULT_WIDTH, getResources()));
            mDroneLeashPath = mMap.addPolyline(flightPath);
        }

        mDroneLeashPath.setPoints(pathPoints);
    }

    @Override
    public void updateMissionPath(PathSource pathSource){
        List<Coord2D> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for(Coord2D coord: pathCoords){
            pathPoints.add(DroneHelper.CoordToLatLang(coord));
        }

        if(missionPath == null){
            PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(MISSION_PATH_DEFAULT_WIDTH);
            missionPath = mMap.addPolyline(pathOptions);
        }

        missionPath.setPoints(pathPoints);
    }

    /**
     * Save the map camera state on a preference file
     * http://stackoverflow.com/questions/16697891/google-maps-android-api-v2-restoring-map-state/16698624#16698624
     */
    @Override
    public void saveCameraPosition() {
        CameraPosition camera = mMap.getCameraPosition();
        mAppPrefs.prefs.edit()
                .putFloat(PREF_LAT, (float) camera.target.latitude)
                .putFloat(PREF_LNG, (float) camera.target.longitude)
                .putFloat(PREF_BEA, camera.bearing)
                .putFloat(PREF_TILT, camera.tilt)
                .putFloat(PREF_ZOOM, camera.zoom)
                .apply();
    }

    @Override
    public void loadCameraPosition() {
        final SharedPreferences settings = mAppPrefs.prefs;

        CameraPosition.Builder camera = new CameraPosition.Builder();
        camera.bearing(settings.getFloat(PREF_BEA, DEFAULT_BEARING));
        camera.tilt(settings.getFloat(PREF_TILT, DEFAULT_TILT));
        camera.zoom(settings.getFloat(PREF_ZOOM, DEFAULT_ZOOM_LEVEL));
        camera.target(new LatLng(settings.getFloat(PREF_LAT, DEFAULT_LATITUDE),
                settings.getFloat(PREF_LNG, DEFAULT_LONGITUDE)));

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera.build()));
    }

    private void setupMap() {
        //Make sure the map is initialized
        MapsInitializer.initialize(getActivity().getApplicationContext());

        mMap = getMap();
        if (isMapLayoutFinished()) {
            // TODO it should wait for the map layout
            // before setting it up, instead of just
            // skipping the setup
            setupMapUI();
            setupMapOverlay();
            setupMapListeners();
        }
    }

    @Override
    public void goToMyLocation(){
        if(mApiClient.isConnected())
            mGoToMyLocationTask.run();
        else{
            mApiClientTasks.add(mGoToMyLocationTask);
        }
    }

    @Override
    public void goToDroneLocation(){
        final float currentZoomLevel = mMap.getCameraPosition().zoom;
        final Coord2D droneLocation = mDrone.GPS.getPosition();
        updateCamera(droneLocation, (int)currentZoomLevel);
    }

    private void setupMapListeners(){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mMapClickListener != null){
                    mMapClickListener.onMapClick(DroneHelper.LatLngToCoord(latLng));
                }
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(mMapLongClickListener != null){
                    mMapLongClickListener.onMapLongClick(DroneHelper.LatLngToCoord(latLng));
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(mMarkerDragListener != null){
                    final MarkerInfo markerInfo = getMarkerInfo(marker);
                    markerInfo.setPosition(DroneHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragStart(markerInfo);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if(mMarkerDragListener != null){
                    final MarkerInfo markerInfo = getMarkerInfo(marker);
                    markerInfo.setPosition(DroneHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDrag(markerInfo);
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if(mMarkerDragListener != null){
                    final MarkerInfo markerInfo = getMarkerInfo(marker);
                    markerInfo.setPosition(DroneHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(mMarkerClickListener != null){
                    return mMarkerClickListener.onMarkerClick(getMarkerInfo(marker));
                }
                return false;
            }
        });
    }

    private void setupMapUI() {
        mMap.setMyLocationEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
    }

    private void setupMapOverlay() {
        if (mAppPrefs.isOfflineMapEnabled()) {
            setupOfflineMapOverlay();
        } else {
            setupOnlineMapOverlay();
        }
    }

    private void setupOnlineMapOverlay() {
        mMap.setMapType(getMapType());
    }

    private int getMapType() {
        String mapType = mAppPrefs.getMapType();

        if (mapType.equalsIgnoreCase(MAP_TYPE_SATELLITE)) {
            return GoogleMap.MAP_TYPE_SATELLITE;
        }
        if (mapType.equalsIgnoreCase(MAP_TYPE_HYBRID)) {
            return GoogleMap.MAP_TYPE_HYBRID;
        }
        if (mapType.equalsIgnoreCase(MAP_TYPE_NORMAL)) {
            return GoogleMap.MAP_TYPE_NORMAL;
        }
        if (mapType.equalsIgnoreCase(MAP_TYPE_TERRAIN)) {
            return GoogleMap.MAP_TYPE_TERRAIN;
        } else {
            return GoogleMap.MAP_TYPE_SATELLITE;
        }
    }

    private void setupOfflineMapOverlay() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(new LocalMapTileProvider()));
        tileOverlay.setZIndex(-1);
        tileOverlay.clearTileCache();
    }

    public void zoomToExtents(List<LatLng> pointsList) {
        if (!pointsList.isEmpty()) {
            LatLngBounds bounds = getBounds(pointsList);
            CameraUpdate animation;
            if (isMapLayoutFinished())
                animation = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            else
                animation = CameraUpdateFactory.newLatLngBounds(bounds, 480,
                        360, 100);
            getMap().animateCamera(animation);
        }
    }

    protected void clearMap() {
        GoogleMap mMap = getMap();
        mMap.clear();
        setupMapOverlay();
    }

    private LatLngBounds getBounds(List<LatLng> pointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : pointsList) {
            builder.include(point);
        }
        return builder.build();
    }

    public double getMapRotation() {
        if (isMapLayoutFinished()) {
            return mMap.getCameraPosition().bearing;
        } else {
            return 0;
        }
    }

    private boolean isMapLayoutFinished() {
        return getMap() != null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google API Client connection failed: " + connectionResult.getErrorCode());
        if(connectionResult.hasResolution()){
            try {
                connectionResult.startResolutionForResult(getActivity(), 0);
            }
            catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Unable to launch the resolution intent.", e);
            }
        }
    }

    /**
     * Used to monitor drone gps location updates if autopan is enabled.
     * {@inheritDoc}
     * @param event event type
     * @param drone drone state
     */
    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch(event){
            case GPS:
                if(mPanMode.get() == AutoPanMode.DRONE){
                    final float currentZoomLevel = mMap.getCameraPosition().zoom;
                    final Coord2D droneLocation = drone.GPS.getPosition();
                    updateCamera(droneLocation, (int)currentZoomLevel);
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "User location changed.");
        if(mPanMode.get() == AutoPanMode.USER){
            updateCamera(DroneHelper.LocationToCoord(location),(int)mMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        for(Runnable task: mApiClientTasks){
            task.run();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google API client connection suspended.");
    }
}
