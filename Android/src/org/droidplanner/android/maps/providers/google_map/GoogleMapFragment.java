package org.droidplanner.android.maps.providers.google_map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.droidplanner.R;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.helpers.LocalMapTileProvider;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

public class GoogleMapFragment extends SupportMapFragment implements DPMap {

	public static final String PREF_MAP_TYPE = "pref_map_type";

	public static final String MAP_TYPE_SATELLITE = "Satellite";
	public static final String MAP_TYPE_HYBRID = "Hybrid";
	public static final String MAP_TYPE_NORMAL = "Normal";
	public static final String MAP_TYPE_TERRAIN = "Terrain";

    private final HashBiMap<MarkerInfo, Marker> mMarkers = HashBiMap.create();

	private GoogleMap mMap;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);

        Bundle args = getArguments();
        if(args != null){
            maxFlightPathSize = args.getInt(EXTRA_MAX_FLIGHT_PATH_SIZE);
        }

		return view;
	}

    @Override
    public void onStart(){
        super.onStart();
        setupMap();
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DroneHelper.CoordToLatLang(coord),
                zoomLevel));
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
        SharedPreferences settings = getActivity().getSharedPreferences("MAP", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(PREF_LAT, (float) camera.target.latitude);
        editor.putFloat(PREF_LNG, (float) camera.target.longitude);
        editor.putFloat(PREF_BEA, camera.bearing);
        editor.putFloat(PREF_TILT, camera.tilt);
        editor.putFloat(PREF_ZOOM, camera.zoom);
        editor.apply();
    }

    @Override
    public void loadCameraPosition() {
        CameraPosition.Builder camera = new CameraPosition.Builder();
        SharedPreferences settings = getActivity().getSharedPreferences("MAP", 0);
        camera.bearing(settings.getFloat(PREF_BEA, 0));
        camera.tilt(settings.getFloat(PREF_TILT, 0));
        camera.zoom(settings.getFloat(PREF_ZOOM, 0));
        camera.target(new LatLng(settings.getFloat(PREF_LAT, 0),settings.getFloat(PREF_LNG, 0)));
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
		return prefs.getBoolean(getString(R.string.pref_advanced_use_offline_maps_key), false);
	}

	private void setupOnlineMapOverlay() {
		mMap.setMapType(getMapType());
	}

	private int getMapType() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		String mapType = prefs.getString(getString(R.string.pref_map_type_key), "");

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
