package org.droidplanner.android.maps.providers.baidu_map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
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

import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.utils.collection.HashBiMap;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;

public class BaiduMapFragment extends SupportMapFragment implements DPMap{ 

    private static final String TAG = BaiduMapFragment.class.getSimpleName();

    private static final IntentFilter mEventFilter = new IntentFilter();
    private LocationClient mBDLocClient;
    public MyBDLocationListenner mBDLocListener = new MyBDLocationListenner();
    private final HashBiMap<MarkerInfo, Marker> mBiMarkersMap = new HashBiMap<MarkerInfo, Marker>();
    private DroidPlannerPrefs mAppPrefs;
    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(AutoPanMode.DISABLED);

    private Polyline mFlightPath;
    private Polyline mMissionPath;
    private Polyline mDroneLeashPath;
    private int maxFlightPathSize;

    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;
    private android.location.LocationListener mLocationListener;

    protected boolean useMarkerClickAsMapClick = false;

    private List<Polygon> polygonsPaths = new ArrayList<Polygon>();

    protected DroidPlannerApp mDpApp;
    private Polygon mFootprintPoly;

    static {
        mEventFilter.addAction(AttributeEvent.GPS_POSITION);
        mEventFilter.addAction(SettingsFragment.ACTION_MAP_ROTATION_PREFERENCE_UPDATED);
        mEventFilter.addAction(com.baidu.mapapi.SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        mEventFilter.addAction(com.baidu.mapapi.SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
    }

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
                    setupMapUI(getBaiduMap());
                    break;
                case com.baidu.mapapi.SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR:
                    Toast.makeText(getActivity().getApplicationContext(),
                            "BaiduMap: Keycode error, please check the keycode in AndroidManifest.xml!", Toast.LENGTH_SHORT).show();
                    break;
                case com.baidu.mapapi.SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR:
                    Toast.makeText(getActivity().getApplicationContext(),
                            "BaiduMap: Network error!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void notifyToLocatorActivity(BDLocation bdloc) {
        if (mLocationListener != null) {
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdloc.getRadius())
                    .direction(100).latitude(bdloc.getLatitude())
                    .longitude(bdloc.getLongitude()).build();

            // convert BDLocation to android.Location
            Location aLoc = new Location("BaiduMap");
            //aLoc.setBearing(); No bearing in baidu map
            aLoc.setLongitude(bdloc.getLongitude());
            aLoc.setLatitude(bdloc.getLatitude());
            mLocationListener.onLocationChanged(aLoc);
        }
    }

    private void updateBDMapStatus(BDLocation location) {
        final BaiduMap map = getBaiduMap();
        final BaiduMapPrefFragment provider = (BaiduMapPrefFragment)(getProvider().getMapProviderPreferences());
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        int mapType =provider.getMapType(context);
        map.setMapType(mapType);

        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .direction(100).latitude(location.getLatitude())
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
			final Activity activity = getActivity();
			if (map == null || location == null || activity == null)
				return;

            updateBDMapStatus(location);
            notifyToLocatorActivity(location);
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }


    // FIXME: Ugly, Duplicated lat long titude convert functions, have to be templated
    private LatLng CoordToLatLang(LatLong coord) {
        return new LatLng(coord.getLatitude(), coord.getLongitude());
    }

    private LatLong LatLngToCoord(LatLng point) {
        return new LatLong((float)point.latitude, (float) point.longitude);
    }

    private LatLong LocationToCoord(BDLocation location) {
        return new LatLong((float) location.getLatitude(), (float) location.getLongitude());
    }

    private int scaleDpToPixels(double value, Resources res) {
        final float scale = res.getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }

    private Drone getDroneApi() { return mDpApp.getDrone(); }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Initialize Baidu Map library
        com.baidu.mapapi.SDKInitializer.initialize(getActivity().getApplicationContext());
        mDpApp = (DroidPlannerApp) activity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        setHasOptionsMenu(true);
        final Context context = getActivity().getApplicationContext();
        final View view = super.onCreateView(inflater, viewGroup, bundle);

        mAppPrefs = DroidPlannerPrefs.getInstance(context);

        final Bundle args = getArguments();
        if (args != null) {
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .registerReceiver(eventReceiver, mEventFilter);

        setupMap();
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                .unregisterReceiver(eventReceiver);

		mBDLocClient.stop();                       // close baidu location service
		getBaiduMap().setMyLocationEnabled(false); // disable location layer
    }

    @Override
    public void clearFlightPath() {
        if (mFlightPath != null) {
            List<LatLng> oldFlightPath = mFlightPath.getPoints();
            oldFlightPath.clear();
            mFlightPath.setPoints(oldFlightPath);
        }
    }

    @Override
    public void downloadMapTiles(MapDownloader mapDownloader, VisibleMapArea mapRegion, int minimumZ, int maximumZ) {
    }

    @Override
    public LatLong getMapCenter() {
        return LatLngToCoord(getBaiduMap().getMapStatus().target);
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

        setAutoPanMode(currentMode, target);
    }

    private void setAutoPanMode(AutoPanMode current, AutoPanMode update) {
        if (mPanMode.compareAndSet(current, update)) {
            switch (current) {
                case DRONE:
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                            .unregisterReceiver(eventReceiver);
                    break;

                case DISABLED:
                default:
                    break;
            }

            switch (update) {
                case DRONE:
                    LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver
                            (eventReceiver, mEventFilter);
                    break;

                case DISABLED:
                default:
                    break;
            }
        }
    }

    @Override
    public DPMapProvider getProvider() {
        return DPMapProvider.BAIDU_MAP;
    }

    @Override
    public void addFlightPathPoint(LatLong coord) {
        final LatLng position = CoordToLatLang(coord);

        if (maxFlightPathSize > 0) {
            if (mFlightPath == null) {
                PolylineOptions flightPathOptions = new PolylineOptions();
                flightPathOptions.color(FLIGHT_PATH_DEFAULT_COLOR)
                        .width(FLIGHT_PATH_DEFAULT_WIDTH).zIndex(1);
                mFlightPath = (Polyline)getBaiduMap().addOverlay(flightPathOptions);
            }

            List<LatLng> oldFlightPath = mFlightPath.getPoints();
            if (oldFlightPath.size() > maxFlightPathSize) {
                oldFlightPath.remove(0);
            }
            oldFlightPath.add(position);
            mFlightPath.setPoints(oldFlightPath); //TODO: need to consider condition - oldFlightPath.size < 2
        }
    }

    @Override
    public void clearMarkers() {
        for (Marker marker : mBiMarkersMap.valueSet()) {
            marker.remove();
        }

        mBiMarkersMap.clear();
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo) {
        updateMarker(markerInfo, markerInfo.isDraggable());
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable) {
        final LatLong coord = markerInfo.getPosition();
        if (coord == null) {
            return;  // return when the drone hasn't received a gps signal yet
        }

        final LatLng position = CoordToLatLang(coord);
        Marker marker = mBiMarkersMap.getValue(markerInfo);
        if (marker == null) {
            generateMarker(markerInfo, position, isDraggable);
        } else {
            updateMarker(marker, markerInfo, position, isDraggable);
        }
    }

    private void generateMarker(MarkerInfo markerInfo, LatLng position, boolean isDraggable) {
		final MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .draggable(isDraggable)
                .alpha(markerInfo.getAlpha())
                .anchor(markerInfo.getAnchorU(), markerInfo.getAnchorV())
                .title(markerInfo.getTitle())
                .rotate(markerInfo.getRotation())
                .flat(markerInfo.isFlat())
			    .visible(markerInfo.isVisible());

        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

		Marker marker = (Marker)getBaiduMap().addOverlay(markerOptions);
        mBiMarkersMap.put(markerInfo, marker);
    }

    private void updateMarker(Marker marker, MarkerInfo markerInfo, LatLng position,
                              boolean isDraggable) {
        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        marker.setAlpha(markerInfo.getAlpha());
        marker.setAnchor(markerInfo.getAnchorU(), markerInfo.getAnchorV());
        //marker.setInfoWindowAnchor(markerInfo.getInfoWindowAnchorU(),
        //markerInfo.getInfoWindowAnchorV());
        marker.setPosition(position);
        marker.setRotate(markerInfo.getRotation());
        //marker.setSnippet(markerInfo.getSnippet());
        marker.setTitle(markerInfo.getTitle());
        marker.setDraggable(isDraggable);
        marker.setFlat(markerInfo.isFlat());
        marker.setVisible(markerInfo.isVisible());
    }

    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos) {
        for (MarkerInfo info : markersInfos) {
            updateMarker(info);
        }
    }

    @Override
    public void updateMarkers(List<MarkerInfo> markersInfos, boolean isDraggable) {
        for (MarkerInfo info : markersInfos) {
            updateMarker(info, isDraggable);
        }
    }

    @Override
    public Set<MarkerInfo> getMarkerInfoList() {
        return new HashSet<MarkerInfo>(mBiMarkersMap.keySet());
    }

    @Override
    public List<LatLong> projectPathIntoMap(List<LatLong> path) {
        List<LatLong> coords = new ArrayList<LatLong>();
        Projection projection = getBaiduMap().getProjection();

        for (LatLong point : path) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getLatitude(), (int) point.getLongitude()));
            coords.add(LatLngToCoord(coord));
        }

        return coords;
    }

    @Override
    public void removeMarkers(Collection<MarkerInfo> markerInfoList) {
        if (markerInfoList == null || markerInfoList.isEmpty()) {
            return;
        }

        for (MarkerInfo markerInfo : markerInfoList) {
            Marker marker = mBiMarkersMap.getValue(markerInfo);
            if (marker != null) {
                marker.remove();
                mBiMarkersMap.removeKey(markerInfo);
            }
        }
    }

    @Override
    public void setMapPadding(int left, int top, int right, int bottom) {
        getBaiduMap().setPadding(left, top, right, bottom);
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
			map.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(CoordToLatLang(coord), zoomLevel));
		}
    }

    @Override
    public void updateCamera(final LatLong coord, final float zoomLevel) {
		final BaiduMap map = getBaiduMap();
        if ( map != null && coord != null) {
            map.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(CoordToLatLang(coord), zoomLevel));
        }
    }

    @Override
    public void updateCameraBearing(float bearing) {
       //final BaiduMap map = getBaiduMap();
       //if ( map != null ) {
       //final MapStatus mapStatus = new MapStatus(DroneHelper.CoordToLatLang
       //    (getMapCenter()), getMapZoomLevel(), 0, bearing);
       // map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
       //}
       return;
    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource) {
        final BaiduMap map = getBaiduMap();
        if (map == null) return;
        // FIXME: updateDroneLeashPath() and updateMissionPath() are almost duplicated, bad smell code, need to be refactored

        List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(CoordToLatLang(coord));
        }

        if (pathPoints.size() < 2) { // BaiduMap Polyline overlay need at least 2 points
            if (mDroneLeashPath != null) {
                mDroneLeashPath.remove();
                mDroneLeashPath = null;
            }
            return;
        }

        if (mDroneLeashPath == null) {
            PolylineOptions flightPath = new PolylineOptions();
            flightPath.color(DRONE_LEASH_DEFAULT_COLOR).width(
                    DroneHelper.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH,
                            getResources()));
            mDroneLeashPath = (Polyline)getBaiduMap().addOverlay(flightPath);
        } else {
            mDroneLeashPath.setPoints(pathPoints);
        }
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

		List<LatLong> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (LatLong coord : pathCoords) {
            pathPoints.add(CoordToLatLang(coord));
        }

        if (pathPoints.size() < 2) { // BaiduMap Polyline overlay need at least 2 points
            if (mMissionPath != null) {
                mMissionPath.remove();
                mMissionPath = null;
            }
            return;
        }

        if (mMissionPath == null) {
            final PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(MISSION_PATH_DEFAULT_WIDTH).points(pathPoints);
            mMissionPath = (Polyline) map.addOverlay(pathOptions);
        } else {
            mMissionPath.setPoints(pathPoints);
        }
    }


    @Override
    public void updatePolygonsPaths(List<List<LatLong>> paths) {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

        for (Polygon poly : polygonsPaths) {
            poly.remove();
        }

        for (List<LatLong> contour : paths) {
            PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.fillColor(POLYGONS_PATH_DEFAULT_COLOR);
			final List<LatLng> pathPoints = new ArrayList<LatLng>(contour.size());
            for (LatLong coord : contour) {
                pathPoints.add(CoordToLatLang(coord));
            }
            pathOptions.points(pathPoints);
            polygonsPaths.add((Polygon)map.addOverlay(pathOptions));
        }

    }

    @Override
    public void addCameraFootprint(FootPrint footprintToBeDraw) {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

        PolygonOptions pathOptions = new PolygonOptions();
		pathOptions.stroke(new Stroke(FOOTPRINT_DEFAULT_WIDTH, FOOTPRINT_DEFAULT_COLOR));
        pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

        final List<LatLng> pathPoints = new ArrayList<LatLng>(footprintToBeDraw.getVertexInGlobalFrame().size());
        for (LatLong coord : footprintToBeDraw.getVertexInGlobalFrame()) {
            pathPoints.add(CoordToLatLang(coord));
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
                .putFloat(PREF_BEA,  mapStatus.rotate)   //FIXME: is bearing equal to rotate in baidu map?
                .putFloat(PREF_TILT, mapStatus.overlook) //FIXME: is tilt equal to overlook in baidu map?
                .putFloat(PREF_ZOOM, mapStatus.zoom).apply();
    }

    @Override
    public void loadCameraPosition() {
        final BaiduMap map = getBaiduMap();
		if (map == null) return;

		final SharedPreferences settings = mAppPrefs.prefs;
		final MapStatus.Builder camera = new MapStatus.Builder();

		camera.rotate(settings.getFloat(PREF_BEA, DEFAULT_BEARING));  //FIXME: is bearing equal to rotate in baidu map?
		camera.overlook(settings.getFloat(PREF_TILT, DEFAULT_TILT));  //FIXME: is tilt equal to overlook in baidu map?
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
                points.add(CoordToLatLang(coord));

            final LatLngBounds bounds = getBounds(points);

			final Activity activity = getActivity();
			if(activity == null)
				return;

			final View rootView = ((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);
			if(rootView == null)
				return;

			final int height = rootView.getHeight();
			final int width = rootView.getWidth();
			Log.d(TAG, String.format(Locale.US, "Screen W %d, H %d", width, height));
			if(height > 0 && width > 0) {
				MapStatusUpdate animation = MapStatusUpdateFactory.newLatLngBounds(bounds, width, height); //, 100); FIXME: What does 100 mean? zoom?
				map.animateMapStatus(animation);
			}
        }
    }

    @Override
    public void zoomToFitMyLocation(final List<LatLong> coords) {
		// FIXME: do something here
	}

    @Override
    public void goToMyLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll"); // set cord type
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
            Toast.makeText(getActivity().getApplicationContext(), R.string.drone_no_location, Toast.LENGTH_SHORT).show();
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
                    mMapClickListener.onMapClick(LatLngToCoord(latLng));
                }
            }

            public boolean onMapPoiClick(MapPoi poi) {
                if (mMapClickListener != null) {
                    // TODO: do nothing
                    return true;
                }
                return false;
            }
        };
        baiduMap.setOnMapClickListener(onMapClickListener);

        baiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick(LatLngToCoord(latLng));
                }
            }
        });

        baiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragStart(markerInfo);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDrag(markerInfo);
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }
        });

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (useMarkerClickAsMapClick) {
                    onMapClickListener.onMapClick(marker.getPosition());
                    return true;
                }

                if (mMarkerClickListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
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

        UiSettings mUiSettings = map.getUiSettings();
        mUiSettings.setCompassEnabled(false);
        mUiSettings.setOverlookingGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
    }

    private void setupMapOverlay(BaiduMap map) {
        final BaiduMapPrefFragment provider = (BaiduMapPrefFragment)(getProvider().getMapProviderPreferences());
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        int mapType =provider.getMapType(context);
        map.setMapType(mapType);
    }

    private void setupOnlineMapOverlay(BaiduMap map) {
        // not used in baidumap
        return;
    }

    private void setupOfflineMapOverlay(BaiduMap map) {
        // not used in baidumap
		return;
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
        return null;
    /*      FIXME: baidu map project doesn't have visible region
        final BaiduMap map = getBaiduMap();
        if(map == null)
            return null;

        final VisibleRegion mapRegion = map.getProjection().getVisibleRegion();
        return new VisibleMapArea(DroneHelper.LatLngToCoord(mapRegion.farLeft),
                DroneHelper.LatLngToCoord(mapRegion.nearLeft),
                DroneHelper.LatLngToCoord(mapRegion.nearRight),
                DroneHelper.LatLngToCoord(mapRegion.farRight));
    */
    }

    @Override
    public void skipMarkerClickEvents(boolean skip) {
        useMarkerClickAsMapClick = skip;
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
					    .stroke(new Stroke(FOOTPRINT_DEFAULT_WIDTH, FOOTPRINT_DEFAULT_COLOR))
                        .fillColor(FOOTPRINT_FILL_COLOR);
                List<LatLng> list = new ArrayList<LatLng>();
                for (LatLong vertex : pathPoints) {
                    list.add(CoordToLatLang(vertex));
                }
				pathOptions.points(list);
                mFootprintPoly = (Polygon)getBaiduMap().addOverlay(pathOptions);
            } else {
                List<LatLng> list = new ArrayList<LatLng>();
                for (LatLong vertex : pathPoints) {
                    list.add(CoordToLatLang(vertex));
                }
                mFootprintPoly.setPoints(list);
            }
        }

    }
}
