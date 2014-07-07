package org.droidplanner.android.maps.providers.osm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.HashBiMap;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This fragment abstracts the use and interaction with an OpenStreetMap view.
 */
public class OSMapFragment extends Fragment implements DPMap {

    private final HashBiMap<MarkerInfo, Marker> mMarkers = HashBiMap.create();

    private final Marker.OnMarkerClickListener mMarkerClickHandler = new Marker
            .OnMarkerClickListener() {

        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            if (mMarkerClickListener != null) {
                return mMarkerClickListener.onMarkerClick(getMarkerInfo(marker));
            }
            return false;
        }
    };

    private final Marker.OnMarkerDragListener mMarkerDragHandler = new Marker.OnMarkerDragListener() {

        @Override
        public void onMarkerDrag(Marker marker) {
            if (mMarkerDragListener != null) {
                final MarkerInfo markerInfo = getMarkerInfo(marker);
                markerInfo.setPosition(DroneHelper.GeoPointToCoord(marker.getPosition()));
                mMarkerDragListener.onMarkerDrag(markerInfo);
            }
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            if (mMarkerDragListener != null) {
                final MarkerInfo markerInfo = getMarkerInfo(marker);
                markerInfo.setPosition(DroneHelper.GeoPointToCoord(marker.getPosition()));
                mMarkerDragListener.onMarkerDragEnd(markerInfo);
            }
        }

        @Override
        public void onMarkerDragStart(Marker marker) {
            if (mMarkerDragListener != null) {
                final MarkerInfo markerInfo = getMarkerInfo(marker);
                markerInfo.setPosition(DroneHelper.GeoPointToCoord(marker.getPosition()));
                mMarkerDragListener.onMarkerDragStart(markerInfo);
            }
        }
    };

    /**
     * osmdroid MapView handle.
     */
    private MapView mMapView;

    private Drone mDrone;
    private DroidPlannerPrefs mAppPrefs;

    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference(AutoPanMode.DISABLED);

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;

    private Polyline mFlightPath;
    private Polyline mMissionPath;
    private Polyline mDroneLeashPath;
    private int mMaxFlightPathSize;

    /*
    DP Map listeners
     */
    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_osmap, container, false);
        mMapView = (MapView) view.findViewById(R.id.osm_mapview);

        final Bundle args = getArguments();
        if (args != null) {
            mMaxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

        final Activity activity = getActivity();
        mDrone = ((DroidPlannerApp)activity.getApplication()).getDrone();
        mAppPrefs = new DroidPlannerPrefs(activity.getApplicationContext());

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        setupMapUI();
    }

    private void setupMapUI() {
        final Context context = getActivity();

        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider
                (context), mMapView);
        mLocationOverlay = new MyLocationNewOverlay(context, new GpsMyLocationProvider(context),
                mMapView);
        final RotationGestureOverlay rotationOverlay = new RotationGestureOverlay(context,
                mMapView);
        rotationOverlay.setEnabled(true);

        final MapEventsOverlay eventsOverlay = new MapEventsOverlay(context,
                new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint iGeoPoint) {
                if (mMapClickListener != null) {
                    mMapClickListener.onMapClick(DroneHelper.GeoPointToCoord(iGeoPoint));
                    return true;
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint iGeoPoint) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick(DroneHelper.GeoPointToCoord(iGeoPoint));
                    return true;
                }
                return false;
            }
        });

        mMapView.setUseSafeCanvas(true);
        mMapView.setMinZoomLevel(4);

        mMapView.setBuiltInZoomControls(false);
        mMapView.setMultiTouchControls(true);

        List<Overlay> mapOverlays = mMapView.getOverlays();
        mapOverlays.add(mLocationOverlay);
        mapOverlays.add(mCompassOverlay);
        mapOverlays.add(rotationOverlay);
        mapOverlays.add(eventsOverlay);

        mMapView.invalidate();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationOverlay.disableMyLocation();
        mCompassOverlay.disableCompass();
    }

    @Override
    public void onResume() {
        super.onResume();

        final ITileSource tileSource = TileSourceFactory.getTileSource(TileSourceFactory
                .DEFAULT_TILE_SOURCE.name());
        mMapView.setTileSource(tileSource);

        mLocationOverlay.enableMyLocation();
        mCompassOverlay.enableCompass();
    }

    @Override
    public void cleanMarkers() {
        for (Map.Entry<MarkerInfo, Marker> entry : mMarkers.entrySet()) {
            Marker marker = entry.getValue();
            marker.remove(mMapView);
            marker.setOnMarkerClickListener(null);
            marker.setOnMarkerDragListener(null);
        }

        mMarkers.clear();
        mMapView.invalidate();
    }

    @Override
    public void clearFlightPath() {
        if (mFlightPath != null) {
            mFlightPath.clearPath();
            mMapView.invalidate();
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
                    mLocationOverlay.disableFollowLocation();
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
                    mLocationOverlay.enableFollowLocation();
                    break;

                case DISABLED:
                default:
                    break;
            }
        }
    }

    @Override
    public DPMapProvider getProvider(){
        return DPMapProvider.OPEN_STREET_MAP;
    }

    @Override
    public void goToMyLocation(){
        final int currentZoomLevel = mMapView.getZoomLevel();
        final GeoPoint myLocation = mLocationOverlay.getMyLocation();
        if(myLocation != null){
            updateCamera(DroneHelper.GeoPointToCoord(myLocation), currentZoomLevel);
        }
    }

    @Override
    public void goToDroneLocation(){
        final int currentZoomLevel = mMapView.getZoomLevel();
        final Coord2D droneLocation = mDrone.GPS.getPosition();
        updateCamera(droneLocation, currentZoomLevel);
    }

    @Override
    public void addFlightPathPoint(Coord2D coord) {
        final GeoPoint position = DroneHelper.CoordToGeoPoint(coord);
        if (mMaxFlightPathSize > 0) {
            if (mFlightPath == null) {
                mFlightPath = new Polyline(getActivity().getApplicationContext());
                mFlightPath.setColor(FLIGHT_PATH_DEFAULT_COLOR);
                mFlightPath.setWidth(FLIGHT_PATH_DEFAULT_WIDTH);
                mMapView.getOverlays().add(mFlightPath);
            }

            List<GeoPoint> oldFlightPath = mFlightPath.getPoints();
            if (oldFlightPath.size() > mMaxFlightPathSize) {
                oldFlightPath.remove(0);
            }

            oldFlightPath.add(position);
            mFlightPath.setPoints(oldFlightPath);
            mMapView.invalidate();
        }
    }

    /**
     * Used to retrieve the info for the given marker.
     *
     * @param marker marker whose info to retrieve
     * @return marker's info
     */
    private MarkerInfo getMarkerInfo(Marker marker) {
        return mMarkers.inverse().get(marker);
    }

    @Override
    public void loadCameraPosition() {
        final SharedPreferences settings = mAppPrefs.prefs;

        final IMapController mapController = mMapView.getController();
        mapController.setCenter(new GeoPoint(
                settings.getFloat(PREF_LAT, DEFAULT_LATITUDE),
                settings.getFloat(PREF_LNG, DEFAULT_LONGITUDE)));
        mapController.setZoom(settings.getInt(PREF_ZOOM, DEFAULT_ZOOM_LEVEL));
        mMapView.setRotation(settings.getFloat(PREF_BEA, DEFAULT_BEARING));
    }

    @Override
    public List<Coord2D> projectPathIntoMap(List<Coord2D> path) {
        List<Coord2D> coords = new ArrayList<Coord2D>();

        MapView.Projection projection = mMapView.getProjection();

        for(Coord2D point : path){
            IGeoPoint coord = projection.fromPixels((float)point.getX(), (float)point.getY());
            coords.add(DroneHelper.GeoPointToCoord(coord));
        }

        return coords;
    }

    @Override
    public void saveCameraPosition() {
        final IGeoPoint mapCenter = mMapView.getMapCenter();
        mAppPrefs.prefs.edit().putFloat(PREF_LAT, (float) mapCenter.getLatitude())
                .putFloat(PREF_LNG, (float) mapCenter.getLongitude())
                .putFloat(PREF_BEA, mMapView.getRotation())
                .putInt(PREF_ZOOM, mMapView.getZoomLevel())
                .apply();
    }

    /**
     * Nop operation
     * <p/>
     * {@inheritDoc}
     *
     * @param left   the number of pixels of padding to be added on the left of the map.
     * @param top    the number of pixels of padding to be added on the top of the map.
     * @param right  the number of pixels of padding to be added on the right of the map.
     * @param bottom the number of pixels of padding to be added on the bottom of the map.
     */
    @Override
    public void setMapPadding(int left, int top, int right, int bottom) {
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
    public void updateCamera(Coord2D coord, int zoomLevel) {
        if(coord == null){
            return;
        }

        IMapController mapController = mMapView.getController();
        mapController.animateTo(DroneHelper.CoordToGeoPoint(coord));
        mapController.setZoom(zoomLevel);
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo) {
        updateMarker(markerInfo, markerInfo.isDraggable());
    }

    @Override
    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable) {
        final GeoPoint position = DroneHelper.CoordToGeoPoint(markerInfo.getPosition());
        Marker marker = mMarkers.get(markerInfo);
        if (marker == null) {
            marker = new Marker(mMapView);
            marker.setPosition(position);
            mMapView.getOverlays().add(marker);
            mMarkers.put(markerInfo, marker);

            marker.setOnMarkerClickListener(mMarkerClickHandler);
            marker.setOnMarkerDragListener(mMarkerDragHandler);
        }

        //Update the marker
        final Resources res = getResources();
        final Bitmap markerIcon = markerInfo.getIcon(res);
        if(markerIcon != null) {
            marker.setIcon(new BitmapDrawable(res, markerIcon));
        }

        marker.setAlpha(markerInfo.getAlpha());
        marker.setAnchor(markerInfo.getAnchorU(), markerInfo.getAnchorV());
        marker.setInfoWindowAnchor(markerInfo.getInfoWindowAnchorU(),
                markerInfo.getInfoWindowAnchorV());
        marker.setPosition(position);
        marker.setRotation(markerInfo.getRotation());
        marker.setSnippet(markerInfo.getSnippet());
        marker.setTitle(markerInfo.getTitle());
        marker.setDraggable(isDraggable);
        marker.setFlat(markerInfo.isFlat());
        marker.setEnabled(markerInfo.isVisible());

        mMapView.invalidate();
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
    public void updateDroneLeashPath(PathSource pathSource) {
        List<Coord2D> pathCoords = pathSource.getPathPoints();
        final List<GeoPoint> geoPoints = new ArrayList<GeoPoint>(pathCoords.size());
        for (Coord2D coord : pathCoords) {
            geoPoints.add(DroneHelper.CoordToGeoPoint(coord));
        }

        if (mDroneLeashPath == null) {
            mDroneLeashPath = new Polyline(getActivity().getApplicationContext());
            mDroneLeashPath.setColor(DRONE_LEASH_DEFAULT_COLOR);
            mDroneLeashPath.setWidth(DroneHelper.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH,
                    getResources()));
            mMapView.getOverlays().add(mDroneLeashPath);
        }

        mDroneLeashPath.setPoints(geoPoints);
        mMapView.invalidate();
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        List<Coord2D> pathCoords = pathSource.getPathPoints();
        final List<GeoPoint> geoPoints = new ArrayList<GeoPoint>(pathCoords.size());
        for (Coord2D coord : pathCoords) {
            geoPoints.add(DroneHelper.CoordToGeoPoint(coord));
        }

        if (mMissionPath == null) {
            mMissionPath = new Polyline(getActivity().getApplicationContext());
            mMissionPath.setColor(MISSION_PATH_DEFAULT_COLOR);
            mMissionPath.setWidth(MISSION_PATH_DEFAULT_WIDTH);
            mMapView.getOverlays().add(mMissionPath);
        }

        mMissionPath.setPoints(geoPoints);
        mMapView.invalidate();
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
                break;
        }
    }
}
