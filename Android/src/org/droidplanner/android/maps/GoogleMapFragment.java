package org.droidplanner.android.maps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager;
import com.o3dr.services.android.lib.util.googleApi.GoogleApiClientManager.GoogleApiClientTask;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.google_map.DownloadMapboxMapActivity;
import org.droidplanner.android.maps.providers.google_map.GoogleMapPrefConstants;
import org.droidplanner.android.maps.providers.google_map.GoogleMapPrefFragment;
import org.droidplanner.android.maps.providers.google_map.tiles.TileProviderManager;
import org.droidplanner.android.maps.providers.google_map.tiles.arcgis.ArcGISTileProviderManager;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.MapboxTileProviderManager;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.MapboxUtils;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;
import org.droidplanner.android.utils.MapUtils;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

public class GoogleMapFragment extends SupportMapFragment implements DPMap,
    GoogleApiClientManager.ManagerListener {

    private static final long USER_LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final long USER_LOCATION_UPDATE_FASTEST_INTERVAL = 5000; // ms
    private static final float USER_LOCATION_UPDATE_MIN_DISPLACEMENT = 0; // m

    private static final float GO_TO_MY_LOCATION_ZOOM = 17f;

    private static final int ONLINE_TILE_PROVIDER_Z_INDEX = -1;
    private static final int OFFLINE_TILE_PROVIDER_Z_INDEX = -2;

    private static final int GET_DRAGGABLE_FROM_MARKER_INFO = -1;
    private static final int IS_DRAGGABLE = 0;
    private static final int IS_NOT_DRAGGABLE = 1;

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(SettingsFragment.ACTION_MAP_ROTATION_PREFERENCE_UPDATED);
    }

    private final static Api<? extends Api.ApiOptions.NotRequiredOptions>[] apisList = new Api[]{LocationServices.API};

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.GPS_POSITION:
                    if (mPanMode.get() == AutoPanMode.DRONE) {
                        final Drone drone = getDroneApi();
                        if (!drone.isConnected())
                            return;

                        final Gps droneGps = drone.getAttribute(AttributeType.GPS);
                        if (droneGps != null && droneGps.isValid()) {
                            final LatLong droneLocation = droneGps.getPosition();
                            updateCamera(droneLocation);
                        }
                    }
                    break;

                case SettingsFragment.ACTION_MAP_ROTATION_PREFERENCE_UPDATED:
                    getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            setupMapUI(googleMap);
                        }
                    });
                    break;
            }
        }
    };

    private final Map<Marker, MarkerInfo> markersMap = new HashMap<>();
    private final Map<Polyline, PolylineInfo> polylinesMap = new HashMap<>();

    private DroidPlannerPrefs mAppPrefs;

    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(
            AutoPanMode.DISABLED);

    private final Handler handler = new Handler();

    private final LocationCallback locationCb = new LocationCallback() {
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(LocationResult result) {
            super.onLocationResult(result);

            final Location location = result.getLastLocation();
            if (location == null)
                return;

            //Update the user location icon.
            if (userMarker == null) {
                final MarkerOptions options = new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .draggable(false)
                        .flat(true)
                        .visible(true)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location));

                getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        userMarker = googleMap.addMarker(options);
                    }
                });
            } else {
                userMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            if (mPanMode.get() == AutoPanMode.USER) {
                Timber.d("User location changed.");
                updateCamera(MapUtils.locationToCoord(location), (int) getMap().getCameraPosition().zoom);
            }

            if (mLocationListener != null) {
                mLocationListener.onLocationChanged(location);
            }
        }
    };

    private final GoogleApiClientTask mGoToMyLocationTask = new GoogleApiClientTask() {
        @Override
        public void doRun() {
            final Location myLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
            if (myLocation != null) {
                updateCamera(MapUtils.locationToCoord(myLocation), GO_TO_MY_LOCATION_ZOOM);

                if (mLocationListener != null)
                    mLocationListener.onLocationChanged(myLocation);
            }
        }
    };

    private final GoogleApiClientTask mRemoveLocationUpdateTask = new GoogleApiClientTask() {
        @Override
        public void doRun() {
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), locationCb);
        }
    };

    private final GoogleApiClientTask mRequestLocationUpdateTask = new GoogleApiClientTask() {
        @Override
        public void doRun() {
            final LocationRequest locationReq = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(USER_LOCATION_UPDATE_FASTEST_INTERVAL)
                    .setInterval(USER_LOCATION_UPDATE_INTERVAL)
                    .setSmallestDisplacement(USER_LOCATION_UPDATE_MIN_DISPLACEMENT);

            LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), locationReq,
                    locationCb, handler.getLooper());
        }
    };

    private final GoogleApiClientTask requestLastLocationTask = new GoogleApiClientTask() {
        @Override
        protected void doRun() {
            final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
            if (lastLocation != null && mLocationListener != null) {
                mLocationListener.onLocationChanged(lastLocation);
            }
        }
    };

    private GoogleApiClientManager mGApiClientMgr;

    private Marker userMarker;

    private Polyline flightPath;
    private Polyline missionPath;
    private Polyline mDroneLeashPath;
    private boolean showFlightPath;

    /*
     * DP Map listeners
     */
    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;
    private android.location.LocationListener mLocationListener;

    private List<Polygon> polygonsPaths = new ArrayList<>();

    protected DroidPlannerApp dpApp;
    private Polygon footprintPoly;

    /*
    Tile overlay
     */
    private TileOverlay onlineTileOverlay;
    private TileOverlay offlineTileOverlay;

    private TileProviderManager tileProviderManager;

    private final OnMapReadyCallback loadCameraPositionTask = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            final SharedPreferences settings = mAppPrefs.prefs;

            final CameraPosition.Builder camera = new CameraPosition.Builder();
            camera.bearing(settings.getFloat(PREF_BEA, DEFAULT_BEARING));
            camera.tilt(settings.getFloat(PREF_TILT, DEFAULT_TILT));
            camera.zoom(settings.getFloat(PREF_ZOOM, DEFAULT_ZOOM_LEVEL));
            camera.target(new LatLng(settings.getFloat(PREF_LAT, DEFAULT_LATITUDE),
                    settings.getFloat(PREF_LNG, DEFAULT_LONGITUDE)));

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera.build()));
        }
    };

    private final OnMapReadyCallback setupMapTask = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            setupMapUI(googleMap);
            setupMapOverlay(googleMap);
            setupMapListeners(googleMap);
        }
    };

    private LocalBroadcastManager lbm;
    private GoogleMap map;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        setHasOptionsMenu(true);

        final FragmentActivity activity = getActivity();
        final Context context = activity.getApplicationContext();

        lbm = LocalBroadcastManager.getInstance(context);

        final View view = super.onCreateView(inflater, viewGroup, bundle);

        mGApiClientMgr = new GoogleApiClientManager(context, new Handler(), apisList);
        mGApiClientMgr.setManagerListener(this);

        mAppPrefs = DroidPlannerPrefs.getInstance(context);

        final Bundle args = getArguments();
        if (args != null) {
            showFlightPath = args.getBoolean(EXTRA_SHOW_FLIGHT_PATH);
        }

        // Load the map
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        return view;
    }

    private GoogleMap getMap() {
        return map;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGApiClientMgr.start();

        mGApiClientMgr.addTask(mRequestLocationUpdateTask);
        lbm.registerReceiver(eventReceiver, eventFilter);
        setupMap();
    }

    @Override
    public void onStop() {
        super.onStop();

        mGApiClientMgr.addTask(mRemoveLocationUpdateTask);
        lbm.unregisterReceiver(eventReceiver);

        mGApiClientMgr.stopSafely();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_google_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_download_mapbox_map:
                startActivity(new Intent(getContext(), DownloadMapboxMapActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        final MenuItem item = menu.findItem(R.id.menu_download_mapbox_map);
        if(item != null) {
            final boolean isEnabled = shouldShowDownloadMapMenuOption();
            item.setEnabled(isEnabled);
            item.setVisible(isEnabled);
        }

    }

    private boolean shouldShowDownloadMapMenuOption(){
        final Context context = getContext();
        final @GoogleMapPrefConstants.TileProvider String tileProvider = GoogleMapPrefFragment.PrefManager
                .getMapTileProvider(context);

        return (GoogleMapPrefConstants.MAPBOX_TILE_PROVIDER.equals(tileProvider)
            || GoogleMapPrefConstants.ARC_GIS_TILE_PROVIDER.equals(tileProvider))
                && GoogleMapPrefFragment.PrefManager.addDownloadMenuOption(context);
    }

    @Override
    public void clearFlightPath() {
        if (flightPath != null) {
            flightPath.remove();
            flightPath = null;
        }
    }

    @Override
    public void downloadMapTiles(MapDownloader mapDownloader, VisibleMapArea mapRegion, int minimumZ, int maximumZ) {
        if(tileProviderManager == null)
            return;

        tileProviderManager.downloadMapTiles(mapDownloader, mapRegion, minimumZ, maximumZ);
    }

    @Override
    public LatLong getMapCenter() {
        return MapUtils.latLngToCoord(getMap().getCameraPosition().target);
    }

    @Override
    public float getMapZoomLevel() {
        return getMap().getCameraPosition().zoom;
    }

    @Override
    public float getMaxZoomLevel() {
        return getMap().getMaxZoomLevel();
    }

    @Override
    public float getMinZoomLevel() {
        return getMap().getMinZoomLevel();
    }

    @Override
    public void selectAutoPanMode(AutoPanMode target) {
        final AutoPanMode currentMode = mPanMode.get();
        if (currentMode == target)
            return;

        mPanMode.compareAndSet(currentMode, target);
    }

    private Drone getDroneApi() {
        return dpApp.getDrone();
    }

    @Override
    public DPMapProvider getProvider() {
        return DPMapProvider.GOOGLE_MAP;
    }

    @Override
    public void addFlightPathPoint(final LatLongAlt coord) {
        final LatLng position = MapUtils.coordToLatLng(coord);

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                if (showFlightPath) {
                    if (flightPath == null) {
                        PolylineOptions flightPathOptions = new PolylineOptions();
                        flightPathOptions.color(FLIGHT_PATH_DEFAULT_COLOR)
                                .width(FLIGHT_PATH_DEFAULT_WIDTH).zIndex(1);
                        flightPath = googleMap.addPolyline(flightPathOptions);
                    }

                    List<LatLng> oldFlightPath = flightPath.getPoints();
                    oldFlightPath.add(position);
                    flightPath.setPoints(oldFlightPath);
                }
            }
        });
    }

    @Override
    public void clearAll(){
        clearUserMarker();
        clearMarkers();
        clearPolylines();
        clearFlightPath();
        clearMissionPath();
        clearFootPrints();
        clearPolygonPaths();
        clearDroneLeashPath();
        GoogleMap googleMap = getMap();
        if(googleMap != null){
            googleMap.clear();
        }
    }

    private void clearUserMarker(){
        if(userMarker != null){
            userMarker.remove();
            userMarker = null;
        }
    }

    private void clearFootPrints(){
        if(footprintPoly != null){
            footprintPoly.remove();
            footprintPoly = null;
        }
    }

    private void clearPolygonPaths(){
        for(Polygon polygon: polygonsPaths){
            polygon.remove();
        }
        polygonsPaths.clear();
    }

    @Override
    public void clearMarkers() {
        for(MarkerInfo markerInfo : markersMap.values()){
            markerInfo.removeProxyMarker();
        }

        markersMap.clear();
    }

    private void clearDroneLeashPath(){
        if(mDroneLeashPath != null){
            mDroneLeashPath.remove();
            mDroneLeashPath = null;
        }
    }

    private void clearMissionPath(){
        if(missionPath != null){
            missionPath.remove();
            missionPath = null;
        }
    }


    @Override
    public void clearPolylines() {
        for(PolylineInfo info: polylinesMap.values()){
            info.removeProxy();
        }

        polylinesMap.clear();
    }

    private PolylineOptions fromPolylineInfo(PolylineInfo info){
        return new PolylineOptions()
            .addAll(MapUtils.coordToLatLng(info.getPoints()))
            .clickable(info.isClickable())
            .color(info.getColor())
            .geodesic(info.isGeodesic())
            .visible(info.isVisible())
            .width(info.getWidth())
            .zIndex(info.getZIndex());
    }

    private MarkerOptions fromMarkerInfo(MarkerInfo markerInfo, boolean isDraggable){
        final LatLong coord = markerInfo.getPosition();
        if (coord == null) {
            return null;
        }

        final MarkerOptions markerOptions = new MarkerOptions()
            .position(MapUtils.coordToLatLng(coord))
            .draggable(isDraggable)
            .alpha(markerInfo.getAlpha())
            .anchor(markerInfo.getAnchorU(), markerInfo.getAnchorV())
            .infoWindowAnchor(markerInfo.getInfoWindowAnchorU(), markerInfo.getInfoWindowAnchorV())
            .rotation(markerInfo.getRotation())
            .snippet(markerInfo.getSnippet())
            .title(markerInfo.getTitle())
            .flat(markerInfo.isFlat())
            .visible(markerInfo.isVisible());

        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        return markerOptions;
    }

    private MarkerOptions fromMarkerInfo(MarkerInfo markerInfo){
        return fromMarkerInfo(markerInfo, markerInfo.isDraggable());
    }

    @Override
    public void addMarker(final MarkerInfo markerInfo) {
        if (markerInfo == null || markerInfo.isOnMap())
            return;

        final MarkerOptions options = fromMarkerInfo(markerInfo);
        if (options == null)
            return;

        GoogleMap googleMap = getMap();
        if (googleMap != null) {
            Marker marker = googleMap.addMarker(options);
            markerInfo.setProxyMarker(new ProxyMapMarker(marker));
            markersMap.put(marker, markerInfo);
        }
    }

    @Override
    public void addMarkers(final List<MarkerInfo> markerInfoList){
        addMarkers(markerInfoList, GET_DRAGGABLE_FROM_MARKER_INFO);
    }

    @Override
    public void addMarkers(final List<MarkerInfo> markerInfoList, boolean isDraggable){
        addMarkers(markerInfoList, isDraggable ? IS_DRAGGABLE : IS_NOT_DRAGGABLE);
    }

    @Override
    public void addPolyline(final PolylineInfo polylineInfo) {
        if(polylineInfo == null || polylineInfo.isOnMap())
            return;

        final PolylineOptions options = fromPolylineInfo(polylineInfo);

        GoogleMap googleMap = getMap();
        if (googleMap != null) {
            Polyline polyline = googleMap.addPolyline(options);
            polylineInfo.setProxyPolyline(new ProxyMapPolyline(polyline));
            polylinesMap.put(polyline, polylineInfo);
        }
    }

    private void addMarkers(final List<MarkerInfo> markerInfoList, int draggableType) {
        if (markerInfoList == null || markerInfoList.isEmpty())
            return;

        final int infoCount = markerInfoList.size();
        final MarkerOptions[] optionsSet = new MarkerOptions[infoCount];
        for (int i = 0; i < infoCount; i++) {
            MarkerInfo markerInfo = markerInfoList.get(i);
            boolean isDraggable = draggableType == GET_DRAGGABLE_FROM_MARKER_INFO
                ? markerInfo.isDraggable()
                : draggableType == IS_DRAGGABLE;
            optionsSet[i] = markerInfo.isOnMap() ? null : fromMarkerInfo(markerInfo, isDraggable);
        }

        GoogleMap googleMap = getMap();
        if (googleMap != null) {
            for (int i = 0; i < infoCount; i++) {
                MarkerOptions options = optionsSet[i];
                if (options == null)
                    continue;

                Marker marker = googleMap.addMarker(options);
                MarkerInfo markerInfo = markerInfoList.get(i);
                markerInfo.setProxyMarker(new ProxyMapMarker(marker));
                markersMap.put(marker, markerInfo);
            }
        }
    }

    @Override
    public List<LatLong> projectPathIntoMap(List<LatLong> path) {
        List<LatLong> coords = new ArrayList<LatLong>();
        Projection projection = getMap().getProjection();

        for (LatLong point : path) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getLatitude(), (int) point.getLongitude()));
            coords.add(MapUtils.latLngToCoord(coord));
        }

        return coords;
    }

    @Override
    public void removeMarker(MarkerInfo markerInfo){
        if(markerInfo == null || !markerInfo.isOnMap())
            return;

        ProxyMapMarker proxyMarker = (ProxyMapMarker) markerInfo.getProxyMarker();
        markerInfo.removeProxyMarker();
        markersMap.remove(proxyMarker.marker);
    }

    @Override
    public void removeMarkers(Collection<MarkerInfo> markerInfoList) {
        if (markerInfoList == null || markerInfoList.isEmpty()) {
            return;
        }

        for (MarkerInfo markerInfo : markerInfoList) {
            removeMarker(markerInfo);
        }
    }

    @Override
    public void removePolyline(PolylineInfo polylineInfo) {
        if(polylineInfo == null || !polylineInfo.isOnMap())
            return;

        ProxyMapPolyline proxy = (ProxyMapPolyline) polylineInfo.getProxyPolyline();
        polylineInfo.removeProxy();
        polylinesMap.remove(proxy.polyline);
    }

    @Override
    public void setMapPadding(int left, int top, int right, int bottom) {
        getMap().setPadding(left, top, right, bottom);
    }

    @Override
    public void setOnMapClickListener(OnMapClickListener listener) {
        mMapClickListener = listener;
    }

    @Override
    public void setOnMapLongClickListener(OnMapLongClickListener listener) {
        mMapLongClickListener = listener;
    }

    @Override
    public void setOnMarkerDragListener(OnMarkerDragListener listener) {
        mMarkerDragListener = listener;
    }

    @Override
    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        mMarkerClickListener = listener;
    }

    @Override
    public void setLocationListener(android.location.LocationListener receiver) {
        mLocationListener = receiver;

        //Update the listener with the last received location
        if (mLocationListener != null) {
            mGApiClientMgr.addTask(requestLastLocationTask);
        }
    }

    private void updateCamera(final LatLong coord) {
        if (coord != null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    final float zoomLevel = googleMap.getCameraPosition().zoom;
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapUtils.coordToLatLng(coord),
                            zoomLevel));
                }
            });
        }
    }

    @Override
    public void updateCamera(final LatLong coord, final float zoomLevel) {
        if (coord != null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            MapUtils.coordToLatLng(coord), zoomLevel));
                }
            });
        }
    }

    @Override
    public void updateCameraBearing(float bearing) {
        final CameraPosition cameraPosition = new CameraPosition(MapUtils.coordToLatLng
                (getMapCenter()), getMapZoomLevel(), 0, bearing);
        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource) {
        List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(MapUtils.coordToLatLng(coord));
        }

        if (mDroneLeashPath == null) {
            final PolylineOptions flightPath = new PolylineOptions();
            flightPath.color(DRONE_LEASH_DEFAULT_COLOR).width(
                    MapUtils.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH, getResources()));

            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mDroneLeashPath = getMap().addPolyline(flightPath);
                    mDroneLeashPath.setPoints(pathPoints);
                }
            });
        }
        else {
            mDroneLeashPath.setPoints(pathPoints);
        }
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(MapUtils.coordToLatLng(coord));
        }

        if (missionPath == null) {
            final PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(MISSION_PATH_DEFAULT_WIDTH);
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    missionPath = getMap().addPolyline(pathOptions);
                    missionPath.setPoints(pathPoints);
                }
            });
        }
        else {
            missionPath.setPoints(pathPoints);
        }
    }


    @Override
    public void updatePolygonsPaths(List<List<LatLong>> paths) {
        GoogleMap map = getMap();
        if (map == null) {
            return;
        }

        for (Polygon poly : polygonsPaths) {
            poly.remove();
        }

        for (List<LatLong> contour : paths) {
            PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.strokeColor(POLYGONS_PATH_DEFAULT_COLOR).strokeWidth(
                    POLYGONS_PATH_DEFAULT_WIDTH);
            final List<LatLng> pathPoints = new ArrayList<LatLng>(contour.size());
            for (LatLong coord : contour) {
                pathPoints.add(MapUtils.coordToLatLng(coord));
            }
            pathOptions.addAll(pathPoints);
            polygonsPaths.add(map.addPolygon(pathOptions));
        }

    }

    @Override
    public void addCameraFootprint(FootPrint footprintToBeDraw) {
        PolygonOptions pathOptions = new PolygonOptions();
        pathOptions.strokeColor(FOOTPRINT_DEFAULT_COLOR).strokeWidth(FOOTPRINT_DEFAULT_WIDTH);
        pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

        for (LatLong vertex : footprintToBeDraw.getVertexInGlobalFrame()) {
            pathOptions.add(MapUtils.coordToLatLng(vertex));
        }
        getMap().addPolygon(pathOptions);

    }

    /**
     * Save the map camera state on a preference file
     * http://stackoverflow.com/questions
     * /16697891/google-maps-android-api-v2-restoring
     * -map-state/16698624#16698624
     */
    @Override
    public void saveCameraPosition() {
        final GoogleMap googleMap = getMap();
        if(googleMap == null)
            return;

        CameraPosition camera = googleMap.getCameraPosition();
        mAppPrefs.prefs.edit()
                .putFloat(PREF_LAT, (float) camera.target.latitude)
                .putFloat(PREF_LNG, (float) camera.target.longitude)
                .putFloat(PREF_BEA, camera.bearing)
                .putFloat(PREF_TILT, camera.tilt)
                .putFloat(PREF_ZOOM, camera.zoom).apply();
    }

    @Override
    public void loadCameraPosition() {
        getMapAsync(loadCameraPositionTask);
    }

    private void setupMap() {
        // Make sure the map is initialized
        MapsInitializer.initialize(getActivity().getApplicationContext());

        getMapAsync(setupMapTask);
    }

    @Override
    public void zoomToFit(List<LatLong> coords) {
        if (!coords.isEmpty()) {
            final List<LatLng> points = new ArrayList<LatLng>();
            for (LatLong coord : coords)
                points.add(MapUtils.coordToLatLng(coord));

            final LatLngBounds bounds = getBounds(points);
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    final Activity activity = getActivity();
                    if (activity == null)
                        return;

                    final View rootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
                    if (rootView == null)
                        return;

                    final int height = rootView.getHeight();
                    final int width = rootView.getWidth();
                    Timber.d("Screen W %d, H %d", width, height);
                    if (height > 0 && width > 0) {
                        CameraUpdate animation = CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100);
                        googleMap.animateCamera(animation);
                    }
                }
            });
        }
    }

    @Override
    public void zoomToFitMyLocation(final List<LatLong> coords) {
        mGApiClientMgr.addTask(new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final Location myLocation = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
                if (myLocation != null) {
                    final List<LatLong> updatedCoords = new ArrayList<LatLong>(coords);
                    updatedCoords.add(MapUtils.locationToCoord(myLocation));
                    zoomToFit(updatedCoords);
                } else {
                    zoomToFit(coords);
                }
            }
        });
    }

    @Override
    public void goToMyLocation() {
        if (!mGApiClientMgr.addTask(mGoToMyLocationTask)) {
            Timber.e("Unable to add google api client task.");
        }
    }

    @Override
    public void goToDroneLocation() {
        Drone dpApi = getDroneApi();
        if (!dpApi.isConnected())
            return;

        Gps gps = dpApi.getAttribute(AttributeType.GPS);
        if (!gps.isValid()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.drone_no_location, Toast.LENGTH_SHORT).show();
            return;
        }

        final float currentZoomLevel = getMap().getCameraPosition().zoom;
        final LatLong droneLocation = gps.getPosition();
        updateCamera(droneLocation, (int) currentZoomLevel);
    }

    private void setupMapListeners(GoogleMap googleMap) {
        final GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMapClickListener != null) {
                    mMapClickListener.onMapClick(MapUtils.latLngToCoord(latLng));
                }
            }
        };
        googleMap.setOnMapClickListener(onMapClickListener);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick(MapUtils.latLngToCoord(latLng));
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    if(!(markerInfo instanceof GraphicHome)) {
                        markerInfo.setPosition(MapUtils.latLngToCoord(marker.getPosition()));
                        mMarkerDragListener.onMarkerDragStart(markerInfo);
                    }
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    if(!(markerInfo instanceof GraphicHome)) {
                        markerInfo.setPosition(MapUtils.latLngToCoord(marker.getPosition()));
                        mMarkerDragListener.onMarkerDrag(markerInfo);
                    }
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    markerInfo.setPosition(MapUtils.latLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (mMarkerClickListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    if (markerInfo != null)
                        return mMarkerClickListener.onMarkerClick(markerInfo);
                }
                return false;
            }
        });
    }

    private void setupMapUI(GoogleMap map) {
        map.setMyLocationEnabled(false);
        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setRotateGesturesEnabled(mAppPrefs.isMapRotationEnabled());
    }

    private void setupMapOverlay(GoogleMap map) {
        final Context context = getContext();
        if(context == null)
            return;

        final @GoogleMapPrefConstants.TileProvider String tileProvider = GoogleMapPrefFragment.PrefManager.getMapTileProvider(context);
        switch(tileProvider){
            case GoogleMapPrefConstants.GOOGLE_TILE_PROVIDER:
                setupGoogleTileProvider(context, map);
                break;

            case GoogleMapPrefConstants.MAPBOX_TILE_PROVIDER:
                setupMapboxTileProvider(context, map);
                break;

            case GoogleMapPrefConstants.ARC_GIS_TILE_PROVIDER:
                setupArcGISTileProvider(context, map);
                break;
        }
    }

    private void setupGoogleTileProvider(Context context, GoogleMap map){
        //Reset the tile provider manager
        tileProviderManager = null;

        //Remove the mapbox tile providers
        if(offlineTileOverlay != null){
            offlineTileOverlay.remove();
            offlineTileOverlay = null;
        }

        if(onlineTileOverlay != null){
            onlineTileOverlay.remove();
            onlineTileOverlay = null;
        }

        map.setMapType(GoogleMapPrefFragment.PrefManager.getMapType(context));
    }

    private void setupArcGISTileProvider(Context context, GoogleMap map){
        Timber.i("Enabling ArcGIS tile provider.");

        //Remove the default google map layer
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        final GoogleMapPrefFragment.PrefManager prefManager = GoogleMapPrefFragment.PrefManager;
        String selectedMap = prefManager.getArcGISMapType(context);

        if(!(tileProviderManager instanceof ArcGISTileProviderManager)
            || !selectedMap.equals(((ArcGISTileProviderManager) tileProviderManager).getSelectedMap())){

            //Setup the online tile overlay
            if(onlineTileOverlay != null){
                onlineTileOverlay.remove();
                onlineTileOverlay = null;
            }

            tileProviderManager = new ArcGISTileProviderManager(context, selectedMap);
            TileOverlayOptions options = new TileOverlayOptions()
                .tileProvider(tileProviderManager.getOnlineTileProvider())
                .zIndex(ONLINE_TILE_PROVIDER_Z_INDEX);

            onlineTileOverlay = map.addTileOverlay(options);

            //Setup the offline tile overlay
            if(offlineTileOverlay != null){
                offlineTileOverlay.remove();
                offlineTileOverlay = null;
            }

            if(prefManager.isOfflineMapLayerEnabled(context)){
                options = new TileOverlayOptions()
                    .tileProvider(tileProviderManager.getOfflineTileProvider())
                    .zIndex(OFFLINE_TILE_PROVIDER_Z_INDEX);

                offlineTileOverlay = map.addTileOverlay(options);
            }
        }
    }

    private void setupMapboxTileProvider(Context context, GoogleMap map){
        Timber.d("Enabling mapbox tile provider.");

        //Remove the default google map layer.
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        final GoogleMapPrefFragment.PrefManager prefManager = GoogleMapPrefFragment.PrefManager;
        final String mapboxId = prefManager.getMapboxId(context);
        final String mapboxAccessToken = prefManager.getMapboxAccessToken(context);
        final int maxZoomLevel = (int) map.getMaxZoomLevel();

        if (!(tileProviderManager instanceof MapboxTileProviderManager)
            || !mapboxId.equals(((MapboxTileProviderManager) tileProviderManager).getMapboxId())
            || !mapboxAccessToken.equals(((MapboxTileProviderManager) tileProviderManager).getMapboxAccessToken())) {

            //Setup the online tile overlay
            if (onlineTileOverlay != null) {
                onlineTileOverlay.remove();
                onlineTileOverlay = null;
            }

            tileProviderManager = new MapboxTileProviderManager(context, mapboxId, mapboxAccessToken, maxZoomLevel);
            TileOverlayOptions options = new TileOverlayOptions()
                .tileProvider(tileProviderManager.getOnlineTileProvider())
                .zIndex(ONLINE_TILE_PROVIDER_Z_INDEX);

            onlineTileOverlay = map.addTileOverlay(options);

            //Setup the offline tile overlay
            if(offlineTileOverlay != null){
                offlineTileOverlay.remove();
                offlineTileOverlay = null;
            }

            if(prefManager.isOfflineMapLayerEnabled(context)){
                options = new TileOverlayOptions()
                    .tileProvider(tileProviderManager.getOfflineTileProvider())
                    .zIndex(OFFLINE_TILE_PROVIDER_Z_INDEX);

                offlineTileOverlay = map.addTileOverlay(options);
            }
        }

        //Check if the mapbox credentials are valid.
        new AsyncTask<Void, Void, Integer>(){

            @Override
            protected Integer doInBackground(Void... params) {
                final Context context = getContext();
                return MapboxUtils.fetchReferenceTileUrl(context, mapboxId, mapboxAccessToken);
            }

            @Override
            protected void onPostExecute(Integer result){
                if(result != null){
                    switch(result){
                        case HttpURLConnection.HTTP_UNAUTHORIZED:
                        case HttpURLConnection.HTTP_NOT_FOUND:
                            //Invalid mapbox credentials
                            Context context = getContext();
                            if (context != null) {
                                Toast.makeText(context, R.string.alert_invalid_mapbox_credentials, Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }
            }
        }.execute();
    }

    protected void clearMap() {
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();
                setupMapOverlay(googleMap);
            }
        });
    }

    private LatLngBounds getBounds(List<LatLng> pointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : pointsList) {
            builder.include(point);
        }
        return builder.build();
    }

    public double getMapRotation() {
        GoogleMap map = getMap();
        if (map != null) {
            return map.getCameraPosition().bearing;
        } else {
            return 0;
        }
    }

    public VisibleMapArea getVisibleMapArea(){
        final GoogleMap map = getMap();
        if(map == null)
            return null;

        final VisibleRegion mapRegion = map.getProjection().getVisibleRegion();
        return new VisibleMapArea(MapUtils.latLngToCoord(mapRegion.farLeft),
                MapUtils.latLngToCoord(mapRegion.nearLeft),
                MapUtils.latLngToCoord(mapRegion.nearRight),
                MapUtils.latLngToCoord(mapRegion.farRight));
    }

    @Override
    public void updateRealTimeFootprint(FootPrint footprint) {
        List<LatLong> pathPoints = footprint == null
                ? Collections.<LatLong>emptyList()
                : footprint.getVertexInGlobalFrame();

        if (pathPoints.isEmpty()) {
            if (footprintPoly != null) {
                footprintPoly.remove();
                footprintPoly = null;
            }
        } else {
            if (footprintPoly == null) {
                PolygonOptions pathOptions = new PolygonOptions()
                        .strokeColor(FOOTPRINT_DEFAULT_COLOR)
                        .strokeWidth(FOOTPRINT_DEFAULT_WIDTH)
                        .fillColor(FOOTPRINT_FILL_COLOR);

                for (LatLong vertex : pathPoints) {
                    pathOptions.add(MapUtils.coordToLatLng(vertex));
                }
                footprintPoly = getMap().addPolygon(pathOptions);
            } else {
                List<LatLng> list = new ArrayList<LatLng>();
                for (LatLong vertex : pathPoints) {
                    list.add(MapUtils.coordToLatLng(vertex));
                }
                footprintPoly.setPoints(list);
            }
        }

    }

    @Override
    public void onGoogleApiConnectionError(ConnectionResult connectionResult) {
        final Activity activity = getActivity();
        if (activity == null)
            return;

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, 0);
            } catch (IntentSender.SendIntentException e) {
                //There was an error with the resolution intent. Try again.
                if (mGApiClientMgr != null)
                    mGApiClientMgr.start();
            }
        } else {
            onUnavailableGooglePlayServices(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onUnavailableGooglePlayServices(int i) {
        final Activity activity = getActivity();
        if (activity != null) {
            GooglePlayServicesUtil.showErrorDialogFragment(i, getActivity(), 0, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    activity.finish();
                }
            });
        }
    }

    @Override
    public void onManagerStarted() {

    }

    @Override
    public void onManagerStopped() {

    }

    private static class ProxyMapPolyline implements PolylineInfo.ProxyPolyline {

        private final Polyline polyline;

        private ProxyMapPolyline(Polyline polyline) {
            this.polyline = polyline;
        }

        @Override
        public void setPoints(@NotNull List<? extends LatLong> points) {
            polyline.setPoints(MapUtils.coordToLatLng(points));
        }

        @Override
        public void clickable(boolean clickable) {
            polyline.setClickable(clickable);
        }

        @Override
        public void color(int color) {
            polyline.setColor(color);
        }

        @Override
        public void geodesic(boolean geodesic) {
            polyline.setGeodesic(geodesic);
        }

        @Override
        public void visible(boolean visible) {
            polyline.setVisible(visible);
        }

        @Override
        public void width(float width) {
            polyline.setWidth(width);
        }

        @Override
        public void zIndex(float zIndex) {
            polyline.setZIndex(zIndex);
        }

        @Override
        public void remove() {
            polyline.remove();
        }
    }

    /**
     * GoogleMap implementation of the ProxyMarker interface.
     */
    private static class ProxyMapMarker implements MarkerInfo.ProxyMarker {

        private final Marker marker;

        ProxyMapMarker(Marker marker){
            this.marker = marker;
        }

        @Override
        public void setAlpha(float alpha) {
            marker.setAlpha(alpha);
        }

        @Override
        public void setAnchor(float anchorU, float anchorV) {
            marker.setAnchor(anchorU, anchorV);
        }

        @Override
        public void setDraggable(boolean draggable) {
            marker.setDraggable(draggable);
        }

        @Override
        public void setFlat(boolean flat) {
            marker.setFlat(flat);
        }

        @Override
        public void setIcon(Bitmap icon) {
            if(icon != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            }
        }

        @Override
        public void setInfoWindowAnchor(float anchorU, float anchorV) {
            marker.setInfoWindowAnchor(anchorU, anchorV);
        }

        @Override
        public void setPosition(LatLong coord) {
            if(coord != null) {
                marker.setPosition(MapUtils.coordToLatLng(coord));
            }
        }

        @Override
        public void setRotation(float rotation) {
            marker.setRotation(rotation);
        }

        @Override
        public void setSnippet(String snippet) {
            marker.setSnippet(snippet);
        }

        @Override
        public void setTitle(String title) {
            marker.setTitle(title);
        }

        @Override
        public void setVisible(boolean visible) {
            marker.setVisible(visible);
        }

        @Override
        public void removeMarker(){
            marker.remove();
        }

        @Override
        public boolean equals(Object other){
            if(this == other)
                return true;

            if(!(other instanceof ProxyMapMarker))
                return false;

            return this.marker.equals(((ProxyMapMarker) other).marker);
        }

        @Override
        public int hashCode(){
            return this.marker.hashCode();
        }
    }
}
