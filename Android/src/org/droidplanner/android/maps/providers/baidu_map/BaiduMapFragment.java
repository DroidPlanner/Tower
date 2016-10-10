package org.droidplanner.android.maps.providers.baidu_map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.PolylineInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;
import org.droidplanner.android.utils.MapUtils;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BaiduMapFragment extends SupportMapFragment implements DPMap{

    private static final String TAG = BaiduMapFragment.class.getSimpleName();

    private static final int GET_DRAGGABLE_FROM_MARKER_INFO = -1;
    private static final int IS_DRAGGABLE = 0;
    private static final int IS_NOT_DRAGGABLE = 1;

    private static final IntentFilter mEventFilter = new IntentFilter();
    private LocationClient mBDLocClient;
    public MyBDLocationListenner mBDLocListener = new MyBDLocationListenner();

    private final Map<Marker, MarkerInfo> markersMap = new HashMap<>();
    private final Map<Polyline, PolylineInfo> polylinesMap = new HashMap<>();

    private DroidPlannerPrefs mAppPrefs;
    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(AutoPanMode.DISABLED);

    public static final int LEASH_PATH = 0;
    public static final int MISSION_PATH = 1;
    public static final int FLIGHT_PATH = 2;
    private int baseBottomPadding;

    @IntDef({LEASH_PATH, MISSION_PATH, FLIGHT_PATH})
    @Retention(RetentionPolicy.SOURCE)
    private @interface PolyLineType {}

    private boolean showFlightPath;
    private List<LatLng> flightPathPoints = new LinkedList<>();
    private Polyline mFlightPath;
    private Polyline mMissionPath;
    private Polyline mDroneLeashPath;

    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;
    private android.location.LocationListener mLocationListener;

    protected DroidPlannerApp mDpApp;
    private Polygon mFootprintPoly;
    private List<Polygon> mPolygonsPaths = new ArrayList<Polygon>();
    private static final Stroke mFootprintStroke = new Stroke(FOOTPRINT_DEFAULT_WIDTH, FOOTPRINT_DEFAULT_COLOR);

    static {
        mEventFilter.addAction(AttributeEvent.GPS_POSITION);
        mEventFilter.addAction(SettingsFragment.ACTION_MAP_ROTATION_PREFERENCE_UPDATED);
        mEventFilter.addAction(com.baidu.mapapi.SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        mEventFilter.addAction(com.baidu.mapapi.SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
    }

    private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
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
                    setupMapUI(getBaiduMap());
                    break;
                case com.baidu.mapapi.SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR:
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.baidu_map_sdk_initializer_permission_error_message, Toast.LENGTH_LONG).show();
                    break;
                case com.baidu.mapapi.SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR:
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.baidu_map_sdk_initializer_network_error_message, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void notifyToLocatorActivity(BDLocation bdloc) {
        if (mLocationListener != null) {
            // convert BDLocation to android.Location
            Location aLoc = new Location("BaiduMap");
            aLoc.setLongitude(bdloc.getLongitude());
            aLoc.setLatitude(bdloc.getLatitude());
            mLocationListener.onLocationChanged(aLoc);
        }
    }

    private void updateBDMapStatus(BDLocation location) {
        final BaiduMap map = getBaiduMap();
        final BaiduMapPrefFragment provider = (BaiduMapPrefFragment)(getProvider().getMapProviderPreferences());
        final Context context = getActivity().getApplicationContext();

        int mapType = provider.getMapType(context);
        map.setMapType(mapType);

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        map.setMyLocationData(locData);

        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
        map.animateMapStatus(u);
    }

    public class MyBDLocationListenner implements BDLocationListener {
		@Override
        public void onReceiveLocation(BDLocation location) {
			final BaiduMap map = getBaiduMap();
			if (map == null || location == null || getActivity() == null)
				return;

            if (mPanMode.get() == AutoPanMode.USER) {
                updateBDMapStatus(location);
            }
            notifyToLocatorActivity(location);
        }
    }

    private Drone getDroneApi() { return mDpApp.getDrone(); }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Initialize Baidu Map library
        com.baidu.mapapi.SDKInitializer.initialize(activity.getApplicationContext());
        mDpApp = (DroidPlannerApp) activity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        setHasOptionsMenu(true);
        final Context context = getActivity().getApplicationContext();
        final View view = super.onCreateView(inflater, viewGroup, bundle);

        baseBottomPadding = (int) getResources().getDimension(R.dimen.mission_control_bar_height);
        mAppPrefs = DroidPlannerPrefs.getInstance(context);

        final Bundle args = getArguments();
        if (args != null) {
            showFlightPath = args.getBoolean(EXTRA_SHOW_FLIGHT_PATH);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .registerReceiver(mEventReceiver, mEventFilter);

        setupMap();
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .unregisterReceiver(mEventReceiver);

		mBDLocClient.stop();                       // close BaiduMap location service
		getBaiduMap().setMyLocationEnabled(false); // disable location layer
    }

    @Override
    public void clearFlightPath() {
        flightPathPoints.clear();
        if (mFlightPath != null) {
            mFlightPath.remove();
            mFlightPath = null;
        }
    }

    private void clearDroneLeashPath(){
        if(mDroneLeashPath != null){
            mDroneLeashPath.remove();
            mDroneLeashPath = null;
        }
    }

    private void clearMissionPath(){
        if(mMissionPath != null){
            mMissionPath.remove();
            mMissionPath = null;
        }
    }

    @Override
    public void downloadMapTiles(MapDownloader mapDownloader, VisibleMapArea mapRegion, int minimumZ, int maximumZ) {
    }

    @Override
    public LatLong getMapCenter() {
        return MapUtils.baiduLatLngToCoord(getBaiduMap().getMapStatus().target);
    }

    @Override
    public float getMapZoomLevel() {
        return getBaiduMap().getMapStatus().zoom;
    }

    @Override
    public float getMaxZoomLevel() {
        return getBaiduMap().getMaxZoomLevel();
    }

    @Override
    public float getMinZoomLevel() { return getBaiduMap().getMinZoomLevel(); }

    @Override
    public void selectAutoPanMode(AutoPanMode target) {
        final AutoPanMode currentMode = mPanMode.get();
        if (currentMode == target) return;

        mPanMode.compareAndSet(currentMode, target);
    }

    @Override
    public DPMapProvider getProvider() {
        return DPMapProvider.BAIDU_MAP;
    }

    @Override
    public void addFlightPathPoint(LatLongAlt coord) {
        final LatLng position = MapUtils.coordToBaiduLatLng(coord);

        if (showFlightPath) {
            flightPathPoints.add(position);
            if (flightPathPoints.size() >= 2) {
                if (mFlightPath == null) {
                    PolylineOptions flightPathOptions = new PolylineOptions();
                    flightPathOptions.color(FLIGHT_PATH_DEFAULT_COLOR)
                        .width(FLIGHT_PATH_DEFAULT_WIDTH).zIndex(1)
                    .points(flightPathPoints);
                    mFlightPath = (Polyline) getBaiduMap().addOverlay(flightPathOptions);
                } else {
                    mFlightPath.setPoints(flightPathPoints);
                }
            } else {
                if (mFlightPath != null) {
                    mFlightPath.remove();
                    mFlightPath = null;
                }
            }
        }
    }

    @Override
    public void clearAll(){
        clearMarkers();
        clearPolylines();
        clearFlightPath();
        clearMissionPath();
        clearFootPrints();
        clearPolygonPaths();
        clearDroneLeashPath();
        BaiduMap map = getBaiduMap();
        if(map != null){
            map.clear();
        }
    }

    @Override
    public void clearMarkers() {
        for (MarkerInfo marker : markersMap.values()) {
            marker.removeProxyMarker();
        }

        markersMap.clear();
    }

    @Override
    public void addMarker(final MarkerInfo markerInfo) {
        if (markerInfo == null || markerInfo.isOnMap()) {
            return;
        }

        final MarkerOptions options = fromMarkerInfo(markerInfo);
        if (options == null) {
            return;
        }

        Marker marker = (Marker) getBaiduMap().addOverlay(options);
        markerInfo.setProxyMarker(new ProxyMapMarker(marker));
        markersMap.put(marker, markerInfo);
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
        if (polylineInfo == null || polylineInfo.isOnMap()) {
            return;
        }

        final PolylineOptions options = fromPolylineInfo(polylineInfo);
        if (options == null) {
            return;
        }

        Polyline polyline = (Polyline) getBaiduMap().addOverlay(options);
        polylineInfo.setProxyPolyline(new ProxyMapPolyline(polyline));
        polylinesMap.put(polyline, polylineInfo);
    }

    private void addMarkers(final List<MarkerInfo> markerInfoList, int draggableType){
        if(markerInfoList == null || markerInfoList.isEmpty())
            return;

        final int infoCount = markerInfoList.size();
        final MarkerOptions[] optionsSet = new MarkerOptions[infoCount];
        for(int i = 0; i < infoCount; i++){
            MarkerInfo markerInfo = markerInfoList.get(i);
            boolean isDraggable = draggableType == GET_DRAGGABLE_FROM_MARKER_INFO
                ? markerInfo.isDraggable()
                : draggableType == IS_DRAGGABLE;
            optionsSet[i] = markerInfo.isOnMap() ? null : fromMarkerInfo(markerInfo, isDraggable);
        }

        BaiduMap baiduMap = getBaiduMap();
        for (int i = 0; i < infoCount; i++) {
            MarkerOptions options = optionsSet[i];
            if (options == null) {
                continue;
            }

            Marker marker = (Marker) baiduMap.addOverlay(options);
            MarkerInfo markerInfo = markerInfoList.get(i);
            markerInfo.setProxyMarker(new ProxyMapMarker(marker));
            markersMap.put(marker, markerInfo);
        }
    }

    private void clearFootPrints(){
        if(mFootprintPoly != null){
            mFootprintPoly.remove();
            mFootprintPoly = null;
        }
    }

    private void clearPolygonPaths(){
        for(Polygon polygon: mPolygonsPaths){
            polygon.remove();
        }
        mPolygonsPaths.clear();
    }

    @Override
    public void clearPolylines() {
        for(PolylineInfo info: polylinesMap.values()){
            info.removeProxy();
        }

        polylinesMap.clear();
    }

    private PolylineOptions fromPolylineInfo(PolylineInfo info) {
        List<LatLng> points = MapUtils.coordToBaiduLatLng(info.getPoints());
        if (points.size() <= 1) {
            return null;
        }

        return new PolylineOptions()
            .points(points)
            .color(info.getColor())
            .visible(info.isVisible())
            .width((int) info.getWidth())
            .zIndex((int) info.getZIndex());
    }

    private MarkerOptions fromMarkerInfo(MarkerInfo markerInfo, boolean isDraggable) {
        final LatLong coord = markerInfo.getPosition();
        if (coord == null) {
            return null;
        }

        final MarkerOptions markerOptions = new MarkerOptions()
            .position(MapUtils.coordToBaiduLatLng(coord))
            .draggable(isDraggable)
            .alpha(markerInfo.getAlpha())
            .anchor(markerInfo.getAnchorU(), markerInfo.getAnchorV())
            .rotate(markerInfo.getRotation())
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
    public List<LatLong> projectPathIntoMap(List<LatLong> path) {
        List<LatLong> coords = new ArrayList<LatLong>();
        Projection projection = getBaiduMap().getProjection();

        for (LatLong point : path) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getLatitude(), (int) point.getLongitude()));
            coords.add(MapUtils.baiduLatLngToCoord(coord));
        }

        return coords;
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
    public void removeMarker(MarkerInfo markerInfo){
        if(markerInfo == null || !markerInfo.isOnMap())
            return;

        ProxyMapMarker proxyMarker = (ProxyMapMarker) markerInfo.getProxyMarker();
        markerInfo.removeProxyMarker();
        markersMap.remove(proxyMarker.marker);
    }

    @Override
    public void setMapPadding(int left, int top, int right, int bottom) {
        getBaiduMap().setViewPadding(left, top, right, bottom + baseBottomPadding);
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
        if (mBDLocListener != null) {
            BDLocation bdloc = mBDLocClient.getLastKnownLocation();
            updateBDMapStatus(bdloc);
            notifyToLocatorActivity(bdloc);
        }
    }

    private void updateCamera(final LatLong coord) {
		final BaiduMap map = getBaiduMap();
        if ( map != null && coord != null) {
			final float zoomLevel = map.getMapStatus().zoom;
			map.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(MapUtils.coordToBaiduLatLng(coord), zoomLevel));
		}
    }

    @Override
    public void updateCamera(final LatLong coord, final float zoomLevel) {
		final BaiduMap map = getBaiduMap();
        if ( map != null && coord != null) {
            map.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(MapUtils.coordToBaiduLatLng(coord), zoomLevel));
        }
    }

    @Override
    public void updateCameraBearing(float bearing) {
        final BaiduMap map = getBaiduMap();
        if (map == null) return;

        final MapStatus.Builder camera = new MapStatus.Builder(map.getMapStatus());
        camera.rotate(bearing);
        map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(camera.build()));
    }

    private Polyline updatePath(Polyline polyLine, PathSource pathSource, @PolyLineType int polyLineType) {
        final BaiduMap map = getBaiduMap();
        if (map == null || pathSource == null) return polyLine;

        List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(MapUtils.coordToBaiduLatLng(coord));
        }

        if (pathPoints.size() < 2) { // BaiduMap Polyline overlay needs at least 2 points
            if (polyLine != null) {
                polyLine.remove();
                polyLine = null;
            }
            return polyLine;
        }

        if (polyLine == null) {
            final PolylineOptions pathOptions = new PolylineOptions();

            if (polyLineType == LEASH_PATH) {
                pathOptions.color(DRONE_LEASH_DEFAULT_COLOR).width(
                        MapUtils.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH,
                                getResources())).points(pathPoints);
            }else if (polyLineType == MISSION_PATH) {
                pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(
                        MISSION_PATH_DEFAULT_WIDTH).points(pathPoints);
            }
            polyLine = (Polyline)getBaiduMap().addOverlay(pathOptions);
        } else {
            polyLine.setPoints(pathPoints);
        }
        return polyLine;
    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource) {
        @PolyLineType int PolyLineType = LEASH_PATH;
        mDroneLeashPath = updatePath(mDroneLeashPath, pathSource, PolyLineType);
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        @PolyLineType int PolyLineType = MISSION_PATH;
        mMissionPath = updatePath(mMissionPath, pathSource, PolyLineType);
    }

    @Override
    public void updatePolygonsPaths(List<List<LatLong>> paths) {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

        for (Polygon poly : mPolygonsPaths) {
            poly.remove();
        }

        for (List<LatLong> contour : paths) {
            PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.fillColor(POLYGONS_PATH_DEFAULT_COLOR);
			final List<LatLng> pathPoints = new ArrayList<LatLng>(contour.size());
            for (LatLong coord : contour) {
                pathPoints.add(MapUtils.coordToBaiduLatLng(coord));
            }
            pathOptions.points(pathPoints);
            mPolygonsPaths.add((Polygon)map.addOverlay(pathOptions));
        }

    }

    @Override
    public void addCameraFootprint(FootPrint footprintToBeDraw) {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

        PolygonOptions pathOptions = new PolygonOptions();
		pathOptions.stroke(mFootprintStroke);
        pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

        final List<LatLng> pathPoints = new ArrayList<LatLng>(footprintToBeDraw.getVertexInGlobalFrame().size());
        for (LatLong coord : footprintToBeDraw.getVertexInGlobalFrame()) {
            pathPoints.add(MapUtils.coordToBaiduLatLng(coord));
        }
        pathOptions.points(pathPoints);

        map.addOverlay(pathOptions);

    }

    /**
     * Save the map camera state on a preference file
     * http://stackoverflow.com/questions
     * /16697891/google-maps-android-api-v2-restoring
     * -map-state/16698624#16698624
     */
    @Override
    public void saveCameraPosition() {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

		final MapStatus mapStatus = map.getMapStatus();
        mAppPrefs.prefs.edit()
                .putFloat(PREF_LAT, (float) mapStatus.target.latitude)
                .putFloat(PREF_LNG, (float) mapStatus.target.longitude)
                .putFloat(PREF_BEA,  mapStatus.rotate)
                .putFloat(PREF_TILT, mapStatus.overlook)
                .putFloat(PREF_ZOOM, mapStatus.zoom).apply();
    }

    @Override
    public void loadCameraPosition() {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

		final SharedPreferences settings = mAppPrefs.prefs;
		final MapStatus.Builder camera = new MapStatus.Builder();

		camera.rotate(settings.getFloat(PREF_BEA, DEFAULT_BEARING));
		camera.overlook(settings.getFloat(PREF_TILT, DEFAULT_TILT));
		camera.zoom(settings.getFloat(PREF_ZOOM, DEFAULT_ZOOM_LEVEL));
		camera.target(new LatLng(settings.getFloat(PREF_LAT, DEFAULT_LATITUDE),
                settings.getFloat(PREF_LNG, DEFAULT_LONGITUDE)));

		map.setMapStatus(MapStatusUpdateFactory.newMapStatus(camera.build()));
    }

    private void setupMap() {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

  		setupMapUI(map);
		setupMapOverlay(map);
		setupMapListeners(map);
    }

    @Override
    public void zoomToFit(List<LatLong> coords) {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

		if (!coords.isEmpty()) {
            final List<LatLng> points = new ArrayList<LatLng>();
            for (LatLong coord : coords)
                points.add(MapUtils.coordToBaiduLatLng(coord));

            final LatLngBounds bounds = getBounds(points);

			final Activity activity = getActivity();
			if (activity == null)
				return;

			final View rootView = ((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);
			if (rootView == null)
				return;

			final int height = rootView.getHeight();
			final int width = rootView.getWidth();
			Log.d(TAG, String.format(Locale.US, "Screen W %d, H %d", width, height));
			if (height > 0 && width > 0) {
                MapStatusUpdate animation = MapStatusUpdateFactory.newLatLngBounds(bounds, width, height);
                map.animateMapStatus(animation);
			}
        }
    }

    @Override
    public void zoomToFitMyLocation(final List<LatLong> coords) {
		// Not used in Baddu Map yet
    }

    @Override
    public void goToMyLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		mBDLocClient.setLocOption(option);
		mBDLocClient.start();
    }

    @Override
    public void goToDroneLocation() {
        Drone dpApi = getDroneApi();
        if (!dpApi.isConnected())
            return;

        Gps gps = dpApi.getAttribute(AttributeType.GPS);
        if (!gps.isValid()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    R.string.drone_no_location, Toast.LENGTH_SHORT).show();
            return;
        }

        final float currentZoomLevel = getBaiduMap().getMapStatus().zoom;
        final LatLong droneLocation = gps.getPosition();
        updateCamera(droneLocation, (int) currentZoomLevel);
    }

    private void setupMapListeners(BaiduMap baiduMap) {
        final BaiduMap.OnMapClickListener onMapClickListener = new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMapClickListener != null) {
                    mMapClickListener.onMapClick(MapUtils.baiduLatLngToCoord(latLng));
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi poi) {
                return false; // Not used in Tower
            }
        };
        baiduMap.setOnMapClickListener(onMapClickListener);

        baiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick(MapUtils.baiduLatLngToCoord(latLng));
                }
            }
        });

        baiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    if(!(markerInfo instanceof GraphicHome)) {
                        markerInfo.setPosition(MapUtils.baiduLatLngToCoord(marker.getPosition()));
                        mMarkerDragListener.onMarkerDragStart(markerInfo);
                    }
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    if(!(markerInfo instanceof GraphicHome)) {
                        markerInfo.setPosition(MapUtils.baiduLatLngToCoord(marker.getPosition()));
                        mMarkerDragListener.onMarkerDrag(markerInfo);
                    }
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = markersMap.get(marker);
                    markerInfo.setPosition(MapUtils.baiduLatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }
        });

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
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

    private void setupMapUI(BaiduMap map) {
        map.setMyLocationEnabled(true);
        map.setMyLocationConfigeration(new MyLocationConfiguration(LocationMode.NORMAL, true, null));

        mBDLocClient = new LocationClient(getActivity().getApplicationContext());
		mBDLocClient.registerLocationListener(mBDLocListener);

        // Hide the zoom control
        map.setViewPadding(0, 0, 0, baseBottomPadding);
        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setOverlookingGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(mAppPrefs.isMapRotationEnabled());
    }

    private void setupMapOverlay(BaiduMap map) {
        final BaiduMapPrefFragment provider = (BaiduMapPrefFragment)(getProvider().getMapProviderPreferences());
        final Context context = getActivity().getApplicationContext();
        int mapType =provider.getMapType(context);

        map.setMapType(mapType);
    }

    protected void clearMap() {
		final BaiduMap map = getBaiduMap();
		if (map != null) {
		    map.clear();
            setupMapOverlay(map);
		}
    }

    private LatLngBounds getBounds(List<LatLng> pointsList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : pointsList) {
            builder.include(point);
        }
        return builder.build();
    }

    public double getMapRotation() {
        final BaiduMap map = getBaiduMap();
        if (map == null) return 0;

		return map.getMapStatus().rotate;
    }

    public VisibleMapArea getVisibleMapArea(){
        return null; // No visible area interface in BaiduMap yet.
    }

    @Override
    public void updateRealTimeFootprint(FootPrint footprint) {
        List<LatLong> pathPoints = footprint == null
                ? Collections.<LatLong>emptyList()
                : footprint.getVertexInGlobalFrame();

        if (pathPoints.isEmpty()) {
            if (mFootprintPoly != null) {
                mFootprintPoly.remove();
                mFootprintPoly = null;
            }
        } else {
            if (mFootprintPoly == null) {
                PolygonOptions pathOptions = new PolygonOptions()
					    .stroke(mFootprintStroke)
                        .fillColor(FOOTPRINT_FILL_COLOR);
                List<LatLng> list = new ArrayList<LatLng>();
                for (LatLong vertex : pathPoints) {
                    list.add(MapUtils.coordToBaiduLatLng(vertex));
                }
				pathOptions.points(list);
                mFootprintPoly = (Polygon)getBaiduMap().addOverlay(pathOptions);
            } else {
                List<LatLng> list = new ArrayList<LatLng>();
                for (LatLong vertex : pathPoints) {
                    list.add(MapUtils.coordToBaiduLatLng(vertex));
                }
                mFootprintPoly.setPoints(list);
            }
        }

    }

    private static class ProxyMapPolyline implements PolylineInfo.ProxyPolyline {

        private final Polyline polyline;

        private ProxyMapPolyline(Polyline polyline) {
            this.polyline = polyline;
        }

        @Override
        public void setPoints(@NotNull List<? extends LatLong> points) {
            polyline.setPoints(MapUtils.coordToBaiduLatLng(points));
        }

        @Override
        public void clickable(boolean clickable) {
        }

        @Override
        public void color(int color) {
            polyline.setColor(color);
        }

        @Override
        public void geodesic(boolean geodesic) {
        }

        @Override
        public void visible(boolean visible) {
            polyline.setVisible(visible);
        }

        @Override
        public void width(float width) {
            polyline.setWidth((int) width);
        }

        @Override
        public void zIndex(float zIndex) {
            polyline.setZIndex((int) zIndex);
        }

        @Override
        public void remove() {
            polyline.remove();
        }
    }

    /**
     * BaiduMap implementation of the ProxyMarker interface.
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
        }

        @Override
        public void setPosition(LatLong coord) {
            if(coord != null) {
                marker.setPosition(MapUtils.coordToBaiduLatLng(coord));
            }
        }

        @Override
        public void setRotation(float rotation) {
            marker.setRotate(rotation);
        }

        @Override
        public void setSnippet(String snippet) {

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