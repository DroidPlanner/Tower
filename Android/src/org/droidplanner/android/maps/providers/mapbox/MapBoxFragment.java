package org.droidplanner.android.maps.providers.mapbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.collect.HashBiMap;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay.TrackingMode;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.mapbox.mapboxsdk.views.util.Projection;

/**
 * MapBox implementation of the DPMap interface.
 */
public class MapBoxFragment extends Fragment implements DPMap {

	private final HashBiMap<MarkerInfo, Marker> mMarkers = HashBiMap.create();

	private final ItemizedDraggableIconOverlay.OnMarkerDragListener mMarkerDragHandler = new ItemizedDraggableIconOverlay.OnMarkerDragListener() {
		@Override
		public void onMarkerDrag(Marker marker) {
			if (mMarkerDragListener != null) {
				final MarkerInfo markerInfo = getMarkerInfo(marker);
				markerInfo.setPosition(DroneHelper.ILatLngToCoord(marker.getPoint()));
				mMarkerDragListener.onMarkerDrag(markerInfo);
			}
		}

		@Override
		public void onMarkerDragEnd(Marker marker) {
			if (mMarkerDragListener != null) {
				final MarkerInfo markerInfo = getMarkerInfo(marker);
				markerInfo.setPosition(DroneHelper.ILatLngToCoord(marker.getPoint()));
				mMarkerDragListener.onMarkerDragEnd(markerInfo);
			}
		}

		@Override
		public void onMarkerDragStart(Marker marker) {
			if (mMarkerDragListener != null) {
				final MarkerInfo markerInfo = getMarkerInfo(marker);
				markerInfo.setPosition(DroneHelper.ILatLngToCoord(marker.getPoint()));
				mMarkerDragListener.onMarkerDragStart(markerInfo);
			}
		}
	};

	private final MapViewListener mMapViewListener = new MapViewListener() {
		@Override
		public void onShowMarker(MapView pMapView, Marker pMarker) {
		}

		@Override
		public void onHidemarker(MapView pMapView, Marker pMarker) {
		}

		@Override
		public void onTapMarker(MapView pMapView, final Marker pMarker) {
			if (mMarkerClickListener != null) {
				mMarkerClickListener.onMarkerClick(getMarkerInfo(pMarker));
			}
		}

		@Override
		public void onLongPressMarker(MapView pMapView, Marker pMarker) {
		}

		@Override
		public void onTapMap(MapView pMapView, final ILatLng pPosition) {
			if (mMapClickListener != null) {
				mMapClickListener.onMapClick(DroneHelper.ILatLngToCoord(pPosition));
			}
		}

		@Override
		public void onLongPressMap(MapView pMapView, final ILatLng pPosition) {
			if (mMapLongClickListener != null) {
				mMapLongClickListener.onMapLongClick(DroneHelper.ILatLngToCoord(pPosition));
			}
		}
	};

	private Drone mDrone;
	private DroidPlannerPrefs mPrefs;

	private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(
			AutoPanMode.DISABLED);

	/**
	 * Mapbox map view handle
	 */
	private MapView mMapView;
	private TrackingMode mUserLocationTrackingMode = TrackingMode.NONE;
	private ItemizedDraggableIconOverlay mMarkersOverlay;

	private PathOverlay mFlightPath;
	private PathOverlay mMissionPath;
	private PathOverlay mDroneLeashPath;

	/**
	 * Listeners
	 */
	private OnMapClickListener mMapClickListener;
	private OnMapLongClickListener mMapLongClickListener;
	private OnMarkerDragListener mMarkerDragListener;
	private OnMarkerClickListener mMarkerClickListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mapbox, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Activity activity = getActivity();
		mDrone = ((DroidPlannerApp) activity.getApplication()).getDrone();
		mPrefs = new DroidPlannerPrefs(activity.getApplicationContext());
		mMapView = (MapView) view.findViewById(R.id.mapbox_mapview);
	}

	@Override
	public void onStart() {
		super.onStart();
		setupMap();

		mMapView.setUserLocationEnabled(true);
		final UserLocationOverlay userLocation = mMapView.getUserLocationOverlay();
		if (userLocation != null) {
			userLocation.setDrawAccuracyEnabled(true);

			if (mUserLocationTrackingMode == TrackingMode.NONE) {
				userLocation.disableFollowLocation();
			} else {
				userLocation.enableFollowLocation();
			}
		}
	}

	private void setupMap() {
		// TODO complete
		mMapView.setMapViewListener(mMapViewListener);

		final RotationGestureOverlay rotationOverlay = new RotationGestureOverlay(mMapView);
		rotationOverlay.setEnabled(false);

		mMapView.getOverlays().add(rotationOverlay);
		resetMarkersOverlay();
	}

	private void resetMarkersOverlay() {
		if (mMarkersOverlay != null) {
			mMarkersOverlay.removeAllItems();
			mMapView.removeOverlay(mMarkersOverlay);
		}

		mMarkersOverlay = new ItemizedDraggableIconOverlay(getActivity().getApplicationContext(),
				new ArrayList<Marker>(), new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
					@Override
					public boolean onItemSingleTapUp(int index, Marker item) {
						mMapView.selectMarker(item);
						return true;
					}

					@Override
					public boolean onItemLongPress(int index, Marker item) {
						mMapViewListener.onLongPressMarker(mMapView, item);
						return true;
					}
				});
		mMarkersOverlay.setMarkerDragListener(mMarkerDragHandler);
		mMapView.addItemizedOverlay(mMarkersOverlay);
	}

	@Override
	public void onStop() {
		super.onStop();

		final UserLocationOverlay userLocation = mMapView.getUserLocationOverlay();
		if (userLocation != null) {
			userLocation.disableFollowLocation();
		}

		mMapView.setUserLocationEnabled(false);
	}

	/**
	 * Used to retrieve the info for the given marker.
	 * 
	 * @param marker
	 *            marker whose info to retrieve
	 * @return marker's info
	 */
	private MarkerInfo getMarkerInfo(Marker marker) {
		return mMarkers.inverse().get(marker);
	}

	@Override
	public void addFlightPathPoint(Coord2D coord) {
		if (mFlightPath == null) {
			mFlightPath = new PathOverlay(FLIGHT_PATH_DEFAULT_COLOR, FLIGHT_PATH_DEFAULT_WIDTH);
			mMapView.getOverlays().add(mFlightPath);
		}

		mFlightPath.addPoint(DroneHelper.CoordToLatLng(coord));
	}

	@Override
	public void cleanMarkers() {
		resetMarkersOverlay();
		mMarkers.clear();
		mMapView.invalidate();
	}

	@Override
	public void clearFlightPath() {
		if (mFlightPath != null) {
			mFlightPath.clearPath();
		}
	}

	@Override
	public Coord2D getMapCenter() {
		return DroneHelper.ILatLngToCoord(mMapView.getCenter());
	}

	@Override
	public float getMapZoomLevel() {
		return mMapView.getZoomLevel();
	}

	@Override
	public float getMaxZoomLevel() {
		return mMapView.getMaxZoomLevel();
	}

	@Override
	public float getMinZoomLevel() {
		return mMapView.getMinZoomLevel();
	}

	@Override
	public DPMapProvider getProvider() {
		return DPMapProvider.MAPBOX;
	}

	@Override
	public void goToDroneLocation() {
		if(!mDrone.getGps().isPositionValid()){
			Toast.makeText(getActivity().getApplicationContext(), "No drone location available", Toast.LENGTH_SHORT).show();
			return;
		}
		final float currentZoomLevel = getMapZoomLevel();
		final Coord2D droneLocation = mDrone.getGps().getInterpolatedPosition();
		updateCamera(droneLocation, currentZoomLevel);
	}

	@Override
	public void goToMyLocation() {
		final UserLocationOverlay userLocation = mMapView.getUserLocationOverlay();
		if (userLocation != null) {
			userLocation.goToMyPosition(true);
		}
	}

	@Override
	public void loadCameraPosition() {
		final float centerLat = mPrefs.prefs.getFloat(PREF_LAT, 0);
		final float centerLng = mPrefs.prefs.getFloat(PREF_LNG, 0);
		mMapView.setCenter(new LatLng(centerLat, centerLng));

		final float zoom = mPrefs.prefs.getFloat(PREF_ZOOM, 0);
		mMapView.setZoom(zoom);

		final float rotation = mPrefs.prefs.getFloat(PREF_BEA, 0);
		mMapView.setRotation(rotation);
	}

	@Override
	public List<Coord2D> projectPathIntoMap(List<Coord2D> pathPoints) {
		final List<Coord2D> coords = new ArrayList<Coord2D>();

		Projection projection = mMapView.getProjection();
		for (Coord2D point : pathPoints) {
			ILatLng coord = projection.fromPixels((float) point.getX(), (float) point.getY());
			coords.add(DroneHelper.ILatLngToCoord(coord));
		}
		return coords;
	}

	@Override
	public void saveCameraPosition() {
		SharedPreferences.Editor editor = mPrefs.prefs.edit();
		final ILatLng mapCenter = mMapView.getCenter();
		editor.putFloat(PREF_LAT, (float) mapCenter.getLatitude())
				.putFloat(PREF_LNG, (float) mapCenter.getLongitude())
				.putFloat(PREF_BEA, mMapView.getRotation())
				.putFloat(PREF_ZOOM, mMapView.getZoomLevel()).apply();
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
				mUserLocationTrackingMode = TrackingMode.NONE;
				final UserLocationOverlay userLocation = mMapView.getUserLocationOverlay();
				if (userLocation != null) {
					userLocation.disableFollowLocation();
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
				mUserLocationTrackingMode = TrackingMode.FOLLOW;
				final UserLocationOverlay userLocation = mMapView.getUserLocationOverlay();
				if (userLocation != null) {
					userLocation.enableFollowLocation();
				}
				break;

			case DISABLED:
			default:
				break;
			}
		}
	}

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
	public void setOnMarkerClickListener(OnMarkerClickListener listener) {
		mMarkerClickListener = listener;
	}

	@Override
	public void setOnMarkerDragListener(OnMarkerDragListener listener) {
		mMarkerDragListener = listener;
	}

	@Override
	public void updateCamera(Coord2D coord, float zoomLevel) {
		MapController mapController = mMapView.getController();
		if (mapController != null) {
			mapController.setZoomAnimated(zoomLevel, DroneHelper.CoordToLatLng(coord), true, false);
		}
	}

	@Override
	public void updateDroneLeashPath(PathSource pathSource) {
		final List<Coord2D> pathCoords = pathSource.getPathPoints();

		if (mDroneLeashPath == null) {
			mDroneLeashPath = new PathOverlay(DRONE_LEASH_DEFAULT_COLOR, DRONE_LEASH_DEFAULT_WIDTH);
			mMapView.getOverlays().add(mDroneLeashPath);
		}

		mDroneLeashPath.clearPath();
		for (Coord2D coord : pathCoords) {
			mDroneLeashPath.addPoint(DroneHelper.CoordToLatLng(coord));
		}
	}

	@Override
	public void updateMarker(MarkerInfo markerInfo) {
		updateMarker(markerInfo, markerInfo.isDraggable());
	}

	@Override
	public void updateMarker(MarkerInfo markerInfo, boolean isDraggable) {
        //if the drone hasn't received a gps signal yet
        final Coord2D coord = markerInfo.getPosition();
        if(coord == null){
            return;
        }

		final LatLng position = DroneHelper.CoordToLatLng(coord);
		Marker marker = mMarkers.get(markerInfo);
		if (marker == null) {
			marker = new Marker(mMapView, markerInfo.getTitle(), markerInfo.getSnippet(), position);
			mMarkersOverlay.addItem(marker);
			mMarkers.put(markerInfo, marker);
		} else {
			marker.setTitle(markerInfo.getTitle());
			marker.setDescription(markerInfo.getSnippet());
			marker.setPoint(position);
		}

		// Update the marker
		final Resources res = getResources();
		Bitmap markerIcon = markerInfo.getIcon(res);
		if (markerIcon == null) {
			markerIcon = BitmapFactory.decodeResource(res, R.drawable.ic_action_location);
		}

		marker.setIcon(new Icon(new BitmapDrawable(res, markerIcon)));

		marker.setAnchor(new PointF(markerInfo.getAnchorU(), markerInfo.getAnchorV()));
		marker.invalidate();
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
	public void updateMissionPath(PathSource pathSource) {
		final List<Coord2D> pathCoords = pathSource.getPathPoints();

		if (mMissionPath == null) {
			mMissionPath = new PathOverlay(MISSION_PATH_DEFAULT_COLOR, MISSION_PATH_DEFAULT_WIDTH);
			mMapView.getOverlays().add(mMissionPath);
		}

		mMissionPath.clearPath();
		for (Coord2D coord : pathCoords) {
			mMissionPath.addPoint(DroneHelper.CoordToLatLng(coord));
		}
	}

	@Override
	public void zoomToFit(List<Coord2D> coords) {
		if (coords.isEmpty()) {
			return;
		}

		final ArrayList<LatLng> boxCoords = new ArrayList<LatLng>(coords.size());
		for (Coord2D coord : coords) {
			boxCoords.add(DroneHelper.CoordToLatLng(coord));
		}

		final BoundingBox enclosingBounds = BoundingBox.fromLatLngs(boxCoords);
		mMapView.zoomToBoundingBox(enclosingBounds, true, true, true);
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case GPS:
			if (mPanMode.get() == AutoPanMode.DRONE && drone.getGps().isPositionValid()) {
				final float currentZoomLevel = getMapZoomLevel();
				final Coord2D droneLocation = drone.getGps().getInterpolatedPosition();
				updateCamera(droneLocation, currentZoomLevel);
			}
			break;

		default:
			break;
		}
	}
}
