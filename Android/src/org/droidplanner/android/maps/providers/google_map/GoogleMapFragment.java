package org.droidplanner.android.maps.providers.google_map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.helpers.LocalMapTileProvider;
import org.droidplanner.android.utils.GoogleApiClientManager;
import org.droidplanner.android.utils.GoogleApiClientManager.GoogleApiClientTask;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.utils.collection.HashBiMap;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.survey.Footprint;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
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
import android.view.ViewTreeObserver;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class GoogleMapFragment extends SupportMapFragment implements DPMap, LocationListener {

    private static final String TAG = GoogleMapFragment.class.getSimpleName();

    public static final String PREF_MAP_TYPE = "pref_map_type";

    public static final String MAP_TYPE_SATELLITE = "Satellite";
    public static final String MAP_TYPE_HYBRID = "Hybrid";
    public static final String MAP_TYPE_NORMAL = "Normal";
    public static final String MAP_TYPE_TERRAIN = "Terrain";

    // TODO: update the interval based on the user's current activity.
    private static final long USER_LOCATION_UPDATE_INTERVAL = 30000; // ms
    private static final long USER_LOCATION_UPDATE_FASTEST_INTERVAL = 10000; // ms
    private static final float USER_LOCATION_UPDATE_MIN_DISPLACEMENT = 15; // m

    private static final float GO_TO_MY_LOCATION_ZOOM = 19f;


    private final HashBiMap<MarkerInfo, Marker> mBiMarkersMap = new HashBiMap<MarkerInfo, Marker>();

    private Drone mDrone;
    private DroidPlannerPrefs mAppPrefs;

    private final LinkedList<Runnable> onMapLaidOutTasks = new LinkedList<Runnable>();

    private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(
            AutoPanMode.DISABLED);

    private GoogleApiClientTask mGoToMyLocationTask;
    private GoogleApiClientTask mRemoveLocationUpdateTask;
    private GoogleApiClientTask mRequestLocationUpdateTask;

    private GoogleApiClientManager mGApiClientMgr;

    private Polyline flightPath;
    private Polyline missionPath;
    private Polyline mDroneLeashPath;
    private int maxFlightPathSize;

    /*
     * DP Map listeners
     */
    private DPMap.OnMapClickListener mMapClickListener;
    private DPMap.OnMapLongClickListener mMapLongClickListener;
    private DPMap.OnMarkerClickListener mMarkerClickListener;
    private DPMap.OnMarkerDragListener mMarkerDragListener;
    private android.location.LocationListener mLocationListener;

	protected boolean useMarkerClickAsMapClick = false;
    private boolean isMapLayoutFinished = false;

	private List<Polygon> polygonsPaths = new ArrayList<Polygon>();

	private Polygon footprintPoly;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
                             Bundle bundle) {
        final FragmentActivity activity = getActivity();
        final Context context = activity.getApplicationContext();

        final View view = super.onCreateView(inflater, viewGroup, bundle);

        mGApiClientMgr = new GoogleApiClientManager(context, LocationServices.API);

        mGoToMyLocationTask = mGApiClientMgr.new GoogleApiClientTask() {
            @Override
            public void doRun() {
                final Location myLocation = LocationServices.FusedLocationApi
                        .getLastLocation(getGoogleApiClient());
                if (myLocation != null) {
                    updateCamera(DroneHelper.LocationToCoord(myLocation), GO_TO_MY_LOCATION_ZOOM);
                }
            }
        };

        mRemoveLocationUpdateTask = mGApiClientMgr.new GoogleApiClientTask() {
            @Override
            public void doRun() {
                LocationServices.FusedLocationApi
                        .removeLocationUpdates(getGoogleApiClient(), GoogleMapFragment.this);
            }
        };

        mRequestLocationUpdateTask = mGApiClientMgr.new GoogleApiClientTask() {
            @Override
            public void doRun() {
                final LocationRequest locationReq = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setFastestInterval(USER_LOCATION_UPDATE_FASTEST_INTERVAL)
                        .setInterval(USER_LOCATION_UPDATE_INTERVAL)
                        .setSmallestDisplacement(USER_LOCATION_UPDATE_MIN_DISPLACEMENT);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        getGoogleApiClient(), locationReq, GoogleMapFragment.this);

            }
        };

        mDrone = ((DroidPlannerApp) activity.getApplication()).getDrone();
        mAppPrefs = new DroidPlannerPrefs(context);

        final Bundle args = getArguments();
        if (args != null) {
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

        isMapLayoutFinished = false;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (view.getWidth() > 0) {
                    isMapLayoutFinished = true;
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    if(!onMapLaidOutTasks.isEmpty()) {
                        for (Runnable task : onMapLaidOutTasks) {
                            task.run();
                        }
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        isMapLayoutFinished = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGApiClientMgr.start();
        setupMap();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGApiClientMgr.stop();
    }

    @Override
    public void clearFlightPath() {
        if (flightPath != null) {
            List<LatLng> oldFlightPath = flightPath.getPoints();
            oldFlightPath.clear();
            flightPath.setPoints(oldFlightPath);
        }
    }

    @Override
    public Coord2D getMapCenter() {
        return DroneHelper.LatLngToCoord(getMap().getCameraPosition().target);
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

        setAutoPanMode(currentMode, target);
    }

    private void setAutoPanMode(AutoPanMode current, AutoPanMode update) {
        if (mPanMode.compareAndSet(current, update)) {
            switch (current) {
                case DRONE:
                    mDrone.removeDroneListener(this);
                    break;

                case USER:
                    if(!mGApiClientMgr.addTask(mRemoveLocationUpdateTask)){
                        Log.e(TAG, "Unable to add google api client task.");
                    }
                    break;

                case DISABLED:
                default:
                    break;
            }

            switch (update) {
                case DRONE:
                    mDrone.addDroneListener(this);
                    break;

                case USER:
                    if(!mGApiClientMgr.addTask(mRequestLocationUpdateTask)){
                        Log.e(TAG, "Unable to add google api client task.");
                    }
                    break;

                case DISABLED:
                default:
                    break;
            }
        }
    }

    @Override
    public DPMapProvider getProvider() {
        return DPMapProvider.GOOGLE_MAP;
    }

    @Override
    public void addFlightPathPoint(Coord2D coord) {
        final LatLng position = DroneHelper.CoordToLatLang(coord);

        if (maxFlightPathSize > 0) {
            if (flightPath == null) {
                PolylineOptions flightPathOptions = new PolylineOptions();
                flightPathOptions.color(FLIGHT_PATH_DEFAULT_COLOR)
                        .width(FLIGHT_PATH_DEFAULT_WIDTH).zIndex(1);
                flightPath = getMap().addPolyline(flightPathOptions);
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
        // if the drone hasn't received a gps signal yet
        final Coord2D coord = markerInfo.getPosition();
        if (coord == null) {
            return;
        }

        final LatLng position = DroneHelper.CoordToLatLang(coord);
        Marker marker = mBiMarkersMap.getValue(markerInfo);
        if (marker == null) {
            // Generate the marker
            generateMarker(markerInfo, position, isDraggable);
        } else {
            // Update the marker
            updateMarker(marker, markerInfo, position, isDraggable);
        }
    }

    private void generateMarker(MarkerInfo markerInfo, LatLng position, boolean isDraggable) {
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .draggable(isDraggable)
                .alpha(markerInfo.getAlpha())
                .anchor(markerInfo.getAnchorU(), markerInfo.getAnchorV())
                .infoWindowAnchor(markerInfo.getInfoWindowAnchorU(),
                        markerInfo.getInfoWindowAnchorV()).rotation(markerInfo.getRotation())
                .snippet(markerInfo.getSnippet()).title(markerInfo.getTitle())
                .flat(markerInfo.isFlat()).visible(markerInfo.isVisible());

        final Bitmap markerIcon = markerInfo.getIcon(getResources());
        if (markerIcon != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
        }

        Marker marker = getMap().addMarker(markerOptions);
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
        marker.setInfoWindowAnchor(markerInfo.getInfoWindowAnchorU(),
                markerInfo.getInfoWindowAnchorV());
        marker.setPosition(position);
        marker.setRotation(markerInfo.getRotation());
        marker.setSnippet(markerInfo.getSnippet());
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
    public List<Coord2D> projectPathIntoMap(List<Coord2D> path) {
        List<Coord2D> coords = new ArrayList<Coord2D>();
        Projection projection = getMap().getProjection();

        for (Coord2D point : path) {
            LatLng coord = projection.fromScreenLocation(new Point((int) point
                    .getX(), (int) point.getY()));
            coords.add(DroneHelper.LatLngToCoord(coord));
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
    public void setLocationListener(android.location.LocationListener receiver){
        mLocationListener = receiver;

        //Update the listener with the last received location
        if(mLocationListener != null && isResumed()){
            mGApiClientMgr.addTask(mGApiClientMgr.new GoogleApiClientTask() {
                @Override
                protected void doRun() {
                    final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation
                            (getGoogleApiClient());
                    if(lastLocation != null){
                        mLocationListener.onLocationChanged(lastLocation);
                    }
                }
            });
        }
    }

    @Override
    public void updateCamera(Coord2D coord, float zoomLevel) {
        if (coord != null) {
            getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(
                    DroneHelper.CoordToLatLang(coord), zoomLevel));
        }
    }

    @Override
    public void updateCameraBearing(float bearing){
        final CameraPosition cameraPosition = new CameraPosition(DroneHelper.CoordToLatLang
                (getMapCenter()), getMapZoomLevel(), 0, bearing);
        getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void updateDroneLeashPath(PathSource pathSource) {
        List<Coord2D> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (Coord2D coord : pathCoords) {
            pathPoints.add(DroneHelper.CoordToLatLang(coord));
        }

        if (mDroneLeashPath == null) {
            PolylineOptions flightPath = new PolylineOptions();
            flightPath.color(DRONE_LEASH_DEFAULT_COLOR).width(
                    DroneHelper.scaleDpToPixels(DRONE_LEASH_DEFAULT_WIDTH,
                            getResources()));
            mDroneLeashPath = getMap().addPolyline(flightPath);
        }

        mDroneLeashPath.setPoints(pathPoints);
    }

    @Override
    public void updateMissionPath(PathSource pathSource) {
        List<Coord2D> pathCoords = pathSource.getPathPoints();
        final List<LatLng> pathPoints = new ArrayList<LatLng>(pathCoords.size());
        for (Coord2D coord : pathCoords) {
            pathPoints.add(DroneHelper.CoordToLatLang(coord));
        }

        if (missionPath == null) {
            PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(MISSION_PATH_DEFAULT_COLOR).width(
                    MISSION_PATH_DEFAULT_WIDTH);
            missionPath = getMap().addPolyline(pathOptions);
        }

        missionPath.setPoints(pathPoints);
    }
    
    
    @Override
	public void updateRealTimeFootprint(Footprint footprint) {
    	if (footprintPoly == null) {
    		PolygonOptions pathOptions = new PolygonOptions();
    		pathOptions.strokeColor(FOOTPRINT_DEFAULT_COLOR).strokeWidth(FOOTPRINT_DEFAULT_WIDTH);
    		pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

    		for (Coord2D vertex : footprint.getVertexInGlobalFrame()) {
    			pathOptions.add(DroneHelper.CoordToLatLang(vertex));
    		}
    		footprintPoly = getMap().addPolygon(pathOptions);
    	}else{
    		List<LatLng> list = new ArrayList<LatLng>();
    		for (Coord2D vertex : footprint.getVertexInGlobalFrame()) {
    			list.add(DroneHelper.CoordToLatLang(vertex));
    		}
    		footprintPoly.setPoints(list);
    	}
	}

	@Override
    public void updatePolygonsPaths(List<List<Coord2D>> paths){
        for (Polygon poly : polygonsPaths) {
			poly.remove();
		}
        
        for (List<Coord2D> contour : paths) {
        	PolygonOptions pathOptions = new PolygonOptions();
            pathOptions.strokeColor(POLYGONS_PATH_DEFAULT_COLOR).strokeWidth(
                    POLYGONS_PATH_DEFAULT_WIDTH);
            final List<LatLng> pathPoints = new ArrayList<LatLng>(contour.size());
			for (Coord2D coord : contour) {
		            pathPoints.add(DroneHelper.CoordToLatLang(coord));
			}
			pathOptions.addAll(pathPoints);
			polygonsPaths.add(getMap().addPolygon(pathOptions));
		}
        
    }

	@Override
	public void addCameraFootprint(Footprint footprintToBeDraw) {
		PolygonOptions pathOptions = new PolygonOptions();
		pathOptions.strokeColor(FOOTPRINT_DEFAULT_COLOR).strokeWidth(FOOTPRINT_DEFAULT_WIDTH);
		pathOptions.fillColor(FOOTPRINT_FILL_COLOR);

		for (Coord2D vertex : footprintToBeDraw.getVertexInGlobalFrame()) {
			pathOptions.add(DroneHelper.CoordToLatLang(vertex));
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
        CameraPosition camera = getMap().getCameraPosition();
        mAppPrefs.prefs.edit()
                .putFloat(PREF_LAT, (float) camera.target.latitude)
                .putFloat(PREF_LNG, (float) camera.target.longitude)
                .putFloat(PREF_BEA, camera.bearing)
                .putFloat(PREF_TILT, camera.tilt)
                .putFloat(PREF_ZOOM, camera.zoom).apply();
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

        getMap().moveCamera(CameraUpdateFactory.newCameraPosition(camera.build()));
    }

    private void setupMap() {
        // Make sure the map is initialized
        MapsInitializer.initialize(getActivity().getApplicationContext());

        if (isMapLayoutFinished) {
            setupMapUI();
            setupMapOverlay();
            setupMapListeners();
        }
        else{
            postOnMapLaidOutTask(new Runnable() {
                @Override
                public void run() {
                    setupMap();
                }
            });
        }
    }

    private void postOnMapLaidOutTask(Runnable task){
        onMapLaidOutTasks.offer(task);
    }

    @Override
    public void zoomToFit(List<Coord2D> coords) {
        if (!coords.isEmpty()) {
            final List<LatLng> points = new ArrayList<LatLng>();
            for (Coord2D coord : coords)
                points.add(DroneHelper.CoordToLatLang(coord));

            final LatLngBounds bounds = getBounds(points);
            if (isMapLayoutFinished) {
                CameraUpdate animation = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                getMap().animateCamera(animation);
            }
            else {
                postOnMapLaidOutTask(new Runnable() {
                    @Override
                    public void run() {
                        CameraUpdate animation = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                        getMap().animateCamera(animation);
                    }
                });
            }
        }
    }

    @Override
    public void zoomToFitMyLocation(final List<Coord2D> coords) {
        mGApiClientMgr.addTask(mGApiClientMgr.new GoogleApiClientTask() {
            @Override
            protected void doRun() {
                final Location myLocation = LocationServices.FusedLocationApi.getLastLocation
                        (getGoogleApiClient());
                if (myLocation != null) {
                    final List<Coord2D> updatedCoords = new ArrayList<Coord2D>(coords);
                    updatedCoords.add(DroneHelper.LocationToCoord(myLocation));
                    zoomToFit(updatedCoords);
                } else {
                    zoomToFit(coords);
                }
            }
        });
    }

    @Override
    public void goToMyLocation() {
        if(!mGApiClientMgr.addTask(mGoToMyLocationTask)){
            Log.e(TAG, "Unable to add google api client task.");
        }
    }

    @Override
    public void goToDroneLocation() {
        if(!mDrone.getGps().isPositionValid()){
            Toast.makeText(getActivity().getApplicationContext(), R.string.drone_no_location, Toast.LENGTH_SHORT).show();
            return;
        }
        final float currentZoomLevel = getMap().getCameraPosition().zoom;
        final Coord2D droneLocation = mDrone.getGps().getPosition();
        updateCamera(droneLocation, (int) currentZoomLevel);
    }

    private void setupMapListeners() {
        final GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMapClickListener != null) {
                    mMapClickListener.onMapClick(DroneHelper.LatLngToCoord(latLng));
                }
            }
        };
		getMap().setOnMapClickListener(onMapClickListener);

        getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mMapLongClickListener != null) {
                    mMapLongClickListener.onMapLongClick(DroneHelper.LatLngToCoord(latLng));
                }
            }
        });

        getMap().setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(DroneHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragStart(markerInfo);
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(DroneHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDrag(markerInfo);
                }
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (mMarkerDragListener != null) {
                    final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
                    markerInfo.setPosition(DroneHelper.LatLngToCoord(marker.getPosition()));
                    mMarkerDragListener.onMarkerDragEnd(markerInfo);
                }
            }
        });

        getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (useMarkerClickAsMapClick) {
                    onMapClickListener.onMapClick(marker.getPosition());
                    return true;
                }
                if (mMarkerClickListener != null) {
                    return mMarkerClickListener.onMarkerClick(mBiMarkersMap.getKey(marker));
                }
                return false;
            }
        });
    }

    private void setupMapUI() {
        getMap().setMyLocationEnabled(true);
        UiSettings mUiSettings = getMap().getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setCompassEnabled(false);
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
        getMap().setMapType(getMapType());
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
        getMap().setMapType(GoogleMap.MAP_TYPE_NONE);
        TileOverlay tileOverlay = getMap().addTileOverlay(new TileOverlayOptions()
                .tileProvider(new LocalMapTileProvider()));
        tileOverlay.setZIndex(-1);
        tileOverlay.clearTileCache();
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
        if (isMapLayoutFinished) {
            return getMap().getCameraPosition().bearing;
        } else {
            return 0;
        }
    }

    private boolean isMapLayoutFinished() {
        return getMap() != null && getView() != null && getView().getWidth() > 0;
    }

    /**
     * Used to monitor drone gps location updates if autopan is enabled.
     * {@inheritDoc}
     *
     * @param event
     *            event type
     * @param drone
     *            drone state
     */
    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        switch (event) {
            case GPS:
                if (mPanMode.get() == AutoPanMode.DRONE && drone.getGps().isPositionValid()) {
                    final float currentZoomLevel = getMap().getCameraPosition().zoom;
                    final Coord2D droneLocation = drone.getGps().getPosition();
                    updateCamera(droneLocation, currentZoomLevel);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "User location changed.");
        if (mPanMode.get() == AutoPanMode.USER) {
            updateCamera(DroneHelper.LocationToCoord(location),(int) getMap().getCameraPosition().zoom);
        }

        if(mLocationListener != null){
            mLocationListener.onLocationChanged(location);
        }
    }

	@Override
	public void skipMarkerClickEvents(boolean skip) {
		useMarkerClickAsMapClick = skip;		
	}
}
