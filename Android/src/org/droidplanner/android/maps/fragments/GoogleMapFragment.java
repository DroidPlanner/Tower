package org.droidplanner.android.maps.fragments;

import java.util.List;

import org.droidplanner.android.fragments.helpers.MapPath;
import org.droidplanner.android.fragments.helpers.MapProjection;
import org.droidplanner.android.graphic.map.MarkerManager;
import org.droidplanner.android.helpers.LocalMapTileProvider;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

public class GoogleMapFragment extends SupportMapFragment implements DPMap {

    public static final String PACKAGE_NAME = GoogleMapFragment.class.getPackage().getName();

    public static final String EXTRA_MAX_FLIGHT_PATH_SIZE = PACKAGE_NAME + "" +
            ".EXTRA_MAX_FLIGHT_PATH_SIZE";

	public static final String PREF_MAP_TYPE = "pref_map_type";

	public static final String MAP_TYPE_SATELLITE = "Satellite";
	public static final String MAP_TYPE_HYBRID = "Hybrid";
	public static final String MAP_TYPE_NORMAL = "Normal";
	public static final String MAP_TYPE_TERRAIN = "Terrain";

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_WIDTH = 4;

	private GoogleMap mMap;

    private MarkerManager markers;
    private MapPath droneLeashPath;
    private Polyline flightPath;
    private Polyline missionPath;
    private int maxFlightPathSize;

    /*
    Map listeners
     */
    private GoogleMap.OnMapClickListener mMapClickListener;
    private GoogleMap.OnMapLongClickListener mMapLongClickListener;
    private GoogleMap.OnMarkerClickListener mMarkerClickListener;
    private GoogleMap.OnMarkerDragListener mMarkerDragListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

        Bundle args = getArguments();
        if(args != null){
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

		setupMap();

        markers = new MarkerManager(mMap);
        droneLeashPath = new MapPath(mMap, getResources());

		return view;
	}

    public void addFlightPathToMap() {
        PolylineOptions flightPathOptions = new PolylineOptions();
        flightPathOptions.color(0xfffd693f).width(6).zIndex(1);
        flightPath = mMap.addPolyline(flightPathOptions);
    }

    public void clearFlightPath() {
        List<LatLng> oldFlightPath = flightPath.getPoints();
        oldFlightPath.clear();
        flightPath.setPoints(oldFlightPath);
    }

    public void addFlightPathPoint(LatLng position) {
        if (maxFlightPathSize > 0) {
            List<LatLng> oldFlightPath = flightPath.getPoints();
            if (oldFlightPath.size() > maxFlightPathSize) {
                oldFlightPath.remove(0);
            }
            oldFlightPath.add(position);
            flightPath.setPoints(oldFlightPath);
        }
    }

    public void cleanMarkers(){
        markers.clean();
    }

    public MarkerManager.MarkerSource getMarkerSource(Marker marker){
        return markers.getSourceFromMarker(marker);
    }

    public List<Coord2D> projectPathIntoMap(List<Coord2D> path){
        return MapProjection.projectPathIntoMap(path, mMap);
    }

    public void setMapPadding(int left, int top, int right, int bottom){
        mMap.setPadding(left, top, right, bottom);
    }

    public void setOnMapClickListener(GoogleMap.OnMapClickListener listener){
        mMapClickListener = listener;
        if(mMap != null){
            mMap.setOnMapClickListener(mMapClickListener);
        }
    }

    public void setOnMapLongClickListener(GoogleMap.OnMapLongClickListener listener){
        mMapLongClickListener = listener;
        if(mMap != null){
            mMap.setOnMapLongClickListener(mMapLongClickListener);
        }
    }

    public void setOnMarkerDragListener(GoogleMap.OnMarkerDragListener listener){
        mMarkerDragListener = listener;
        if(mMap != null){
            mMap.setOnMarkerDragListener(mMarkerDragListener);
        }
    }

    public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener listener){
        mMarkerClickListener = listener;
        if(mMap != null){
            mMap.setOnMarkerClickListener(mMarkerClickListener);
        }
    }

    public void updateCamera(LatLng coord, int zoomLevel){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, zoomLevel));
    }

    public void updateDroneLeashPath(MapPath.PathSource source){
        droneLeashPath.update(source);
    }

    public void updateMarker(MarkerManager.MarkerSource source, boolean draggable ){
        markers.updateMarker(source, draggable, getActivity().getApplicationContext());
    }

    public void updateMarkers(List<MarkerManager.MarkerSource> sources, boolean draggable){
        markers.updateMarkers(sources, draggable, getActivity().getApplicationContext());
    }

    public void updateMissionPath(List<LatLng> pathPoints){
        if(missionPath == null){
            PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(DEFAULT_COLOR).width(DEFAULT_WIDTH);
            missionPath = mMap.addPolyline(pathOptions);
        }

        missionPath.setPoints(pathPoints);
    }

    /**
     * Save the map camera state on a preference file
     * http://stackoverflow.com/questions/16697891/google-maps-android-api-v2-restoring-map-state/16698624#16698624
     */
    public void saveCameraPosition() {
        CameraPosition camera = mMap.getCameraPosition();
        SharedPreferences settings = getActivity().getSharedPreferences("MAP", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("lat", (float) camera.target.latitude);
        editor.putFloat("lng", (float) camera.target.longitude);
        editor.putFloat("bea", camera.bearing);
        editor.putFloat("tilt", camera.tilt);
        editor.putFloat("zoom", camera.zoom);
        editor.commit();
    }

    public void loadCameraPosition() {
        CameraPosition.Builder camera = new CameraPosition.Builder();
        SharedPreferences settings = getActivity().getSharedPreferences("MAP", 0);
        camera.bearing(settings.getFloat("bea", 0));
        camera.tilt(settings.getFloat("tilt", 0));
        camera.zoom(settings.getFloat("zoom", 0));
        camera.target(new LatLng(settings.getFloat("lat", 0),settings.getFloat("lng", 0)));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera.build()));
    }

	private void setupMap() {
		mMap = getMap();
		if (isMapLayoutFinished()) {
            // TODO it should wait for the map layout
            // before setting it up, instead of just
            // skipping the setup
            setupMapUI();
			setupMapOverlay();
            addFlightPathToMap();
            setupMapListeners();
		}
	}

    public LatLng getMyLocation() {
        if (mMap.getMyLocation() != null) {
            return new LatLng(mMap.getMyLocation().getLatitude(), mMap
                    .getMyLocation().getLongitude());
        } else {
            return null;
        }
    }

    private void setupMapListeners(){
        mMap.setOnMapClickListener(mMapClickListener);
        mMap.setOnMapLongClickListener(mMapLongClickListener);
        mMap.setOnMarkerDragListener(mMarkerDragListener);
        mMap.setOnMarkerClickListener(mMarkerClickListener);
    }

	private void setupMapUI() {
		mMap.setMyLocationEnabled(true);
		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setMyLocationButtonEnabled(true);
		mUiSettings.setCompassEnabled(true);
		mUiSettings.setTiltGesturesEnabled(false);
		mUiSettings.setZoomControlsEnabled(false);
	}

	private void setupMapOverlay() {
		if (isOfflineMapEnabled()) {
			setupOfflineMapOverlay();
		} else {
			setupOnlineMapOverlay();
		}
	}

	private boolean isOfflineMapEnabled() {
		Context context = this.getActivity();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_advanced_use_offline_maps", false);
	}

	private void setupOnlineMapOverlay() {
		mMap.setMapType(getMapType());
	}

	private int getMapType() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		String mapType = prefs.getString(PREF_MAP_TYPE, "");

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

	/*
	 * @Override public void onMapTypeChanged() { setupMap(); }
	 */
}
