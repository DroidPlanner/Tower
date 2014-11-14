package org.droidplanner.android.maps.providers.mapbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.android.utils.collection.HashBiMap;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay.TrackingMode;
import com.mapbox.mapboxsdk.views.MapController;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.mapbox.mapboxsdk.views.util.Projection;
import com.o3dr.android.client.Drone;
import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.FootPrint;
import com.ox3dr.services.android.lib.drone.property.Gps;

/**
 * MapBox implementation of the DPMap interface.
 */
public class MapBoxFragment extends Fragment implements DPMap {

    private static final IntentFilter eventFilter = new IntentFilter(Event.EVENT_GPS);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Drone drone = getDroneApi();
            if(!drone.isConnected())
                return;

            Gps droneGps = drone.getGps();
            if (mPanMode.get() == AutoPanMode.DRONE && droneGps.isValid()) {
                final float currentZoomLevel = getMapZoomLevel();
                final LatLong droneLocation = droneGps.getPosition();
                updateCamera(droneLocation, currentZoomLevel);
            }
        }
    };

	private final HashBiMap<MarkerInfo, Marker> mBiMarkersMap = new HashBiMap<MarkerInfo, Marker>();

	private final ItemizedDraggableIconOverlay.OnMarkerDragListener mMarkerDragHandler = new ItemizedDraggableIconOverlay.OnMarkerDragListener() {
		@Override
		public void onMarkerDrag(Marker marker) {
			if (mMarkerDragListener != null) {
				final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
				markerInfo.setPosition(DroneHelper.ILatLngToCoord(marker.getPoint()));
				mMarkerDragListener.onMarkerDrag(markerInfo);
			}
		}

		@Override
		public void onMarkerDragEnd(Marker marker) {
			if (mMarkerDragListener != null) {
				final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
				markerInfo.setPosition(DroneHelper.ILatLngToCoord(marker.getPoint()));
				mMarkerDragListener.onMarkerDragEnd(markerInfo);
			}
		}

		@Override
		public void onMarkerDragStart(Marker marker) {
			if (mMarkerDragListener != null) {
				final MarkerInfo markerInfo = mBiMarkersMap.getKey(marker);
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
				mMarkerClickListener.onMarkerClick(mBiMarkersMap.getKey(pMarker));
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

	private DroidPlannerPrefs mPrefs;

	private final AtomicReference<AutoPanMode> mPanMode = new AtomicReference<AutoPanMode>(
			AutoPanMode.DISABLED);

	/**
	 * Mapbox map view handle
	 */
	private MapView mMapView;
	private TrackingMode mUserLocationTrackingMode = TrackingMode.NONE;
	private ItemizedDraggableIconOverlay mMarkersOverlay;
    private UserLocationProvider mLocationProvider;

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
    private LocationListener mLocationListener;

    protected DroidPlannerApp dpApp;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_mapbox, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Activity activity = getActivity();
		mPrefs = new DroidPlannerPrefs(activity.getApplicationContext());
		mMapView = (MapView) view.findViewById(R.id.mapbox_mapview);
	}

    private Drone getDroneApi(){
        return dpApp.getDrone();
    }

	@Override
	public void onStart() {
		super.onStart();
		setupMap();
	}

    @Override
    public void onStop() {
        super.onStop();
        enableUserLocation(false);
    }

    private void enableUserLocation(boolean enable){
        if(mLocationProvider == null){
            mLocationProvider = new UserLocationProvider(new GpsLocationProvider(getActivity()
                    .getApplicationContext()), mMapView);
            mLocationProvider.setDrawAccuracyEnabled(true);
            if(mLocationListener != null){
                mLocationProvider.setLocationListener(mLocationListener);
            }

            mMapView.addOverlay(mLocationProvider);
        }

        if(enable) {
            mLocationProvider.enableMyLocation();

            if(mUserLocationTrackingMode == TrackingMode.NONE){
                mLocationProvider.disableFollowLocation();
            }
            else{
                mLocationProvider.enableFollowLocation();
            }
        }
        else{
            mLocationProvider.disableFollowLocation();
            mLocationProvider.disableMyLocation();
        }
    }

	private void setupMap() {
		mMapView.setMapViewListener(mMapViewListener);

		final RotationGestureOverlay rotationOverlay = new RotationGestureOverlay(mMapView);
		rotationOverlay.setEnabled(false);

		mMapView.getOverlays().add(rotationOverlay);
		resetMarkersOverlay();

        enableUserLocation(true);
	}

	private void removeMarkersOverlay(List<Marker> markers) {
		if (mMarkersOverlay != null) {
			mMarkersOverlay.removeItems(markers);
		}
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
	public void addFlightPathPoint(LatLong coord) {
		if (mFlightPath == null) {
			mFlightPath = new PathOverlay(FLIGHT_PATH_DEFAULT_COLOR, FLIGHT_PATH_DEFAULT_WIDTH);
			mMapView.getOverlays().add(mFlightPath);
		}

		mFlightPath.addPoint(DroneHelper.CoordToLatLng(coord));
	}

    @Override
    public void addCameraFootprint(FootPrint footprintToBeDraw) {
        //TODO: to be implemented.
    }

    @Override
	public void clearMarkers() {
		resetMarkersOverlay();
		mBiMarkersMap.clear();
		mMapView.invalidate();
	}

	@Override
	public void clearFlightPath() {
		if (mFlightPath != null) {
			mFlightPath.clearPath();
		}
	}

	@Override
	public LatLong getMapCenter() {
		return DroneHelper.ILatLngToCoord(mMapView.getCenter());
	}

	@Override
	public float getMapZoomLevel() {
		return mMapView.getZoomLevel();
	}

	@Override
	public Set<MarkerInfo> getMarkerInfoList() {
		return new HashSet<MarkerInfo>(mBiMarkersMap.keySet());
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
        Drone dpApi = getDroneApi();
        if(!dpApi.isConnected())
            return;

        Gps gps = dpApi.getGps();
		if (!gps.isValid()) {
			Toast.makeText(getActivity().getApplicationContext(), "No drone location available",
					Toast.LENGTH_SHORT).show();
			return;
		}
		final float currentZoomLevel = getMapZoomLevel();
		final LatLong droneLocation = gps.getPosition();
		updateCamera(droneLocation, currentZoomLevel);
	}

	@Override
	public void goToMyLocation() {
        if(mLocationProvider != null){
            mLocationProvider.goToMyPosition(true);
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
	public List<LatLong> projectPathIntoMap(List<LatLong> pathPoints) {
		final List<LatLong> coords = new ArrayList<LatLong>();

		Projection projection = mMapView.getProjection();
		for (LatLong point : pathPoints) {
			ILatLng coord = projection.fromPixels((float) point.getLatitude(), (float) point.getLongitude());
			coords.add(DroneHelper.ILatLngToCoord(coord));
		}
		return coords;
	}

	@Override
	public void removeMarkers(Collection<MarkerInfo> markerInfoList) {
		if (markerInfoList == null || markerInfoList.isEmpty()) {
			return;
		}

		final List<Marker> markersToRemove = new ArrayList<Marker>(markerInfoList.size());
		for (MarkerInfo markerInfo : markerInfoList) {
			final Marker marker = mBiMarkersMap.getValue(markerInfo);
			if (marker != null) {
				markersToRemove.add(marker);
				mBiMarkersMap.removeKey(markerInfo);
			}
		}

		removeMarkersOverlay(markersToRemove);
		mMapView.invalidate();
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
                LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                        .unregisterReceiver(eventReceiver);
				break;

			case USER:
				mUserLocationTrackingMode = TrackingMode.NONE;
                if(mLocationProvider != null){
                    mLocationProvider.disableFollowLocation();
                }
				break;

			case DISABLED:
			default:
				break;
			}

			switch (update) {
			case DRONE:
                LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver
                        (eventReceiver, eventFilter);
				break;

			case USER:
				mUserLocationTrackingMode = TrackingMode.FOLLOW;
                if(mLocationProvider != null){
                    mLocationProvider.enableFollowLocation();
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
    public void setLocationListener(LocationListener listener){
        mLocationListener = listener;
        if(mLocationProvider != null) {
            mLocationProvider.setLocationListener(mLocationListener);

            final Location lastLocation = mLocationProvider.getLastFix();
            if(lastLocation != null) {
                mLocationListener.onLocationChanged(lastLocation);
            }
        }
    }

	@Override
	public void updateCamera(LatLong coord, float zoomLevel) {
		MapController mapController = mMapView.getController();
		if (mapController != null) {
			mapController.setZoomAnimated(zoomLevel, DroneHelper.CoordToLatLng(coord), true, false);
		}
	}

    @Override
    public void updateCameraBearing(float bearing){
        mMapView.setMapOrientation(bearing);
    }

	@Override
	public void updateDroneLeashPath(PathSource pathSource) {
		final List<LatLong> pathCoords = pathSource.getPathPoints();

		if (mDroneLeashPath == null) {
			mDroneLeashPath = new PathOverlay(DRONE_LEASH_DEFAULT_COLOR, DRONE_LEASH_DEFAULT_WIDTH);
			mMapView.getOverlays().add(mDroneLeashPath);
		}

		mDroneLeashPath.clearPath();
		for (LatLong coord : pathCoords) {
			mDroneLeashPath.addPoint(DroneHelper.CoordToLatLng(coord));
		}
	}

	@Override
	public void updateMarker(MarkerInfo markerInfo) {
		updateMarker(markerInfo, markerInfo.isDraggable());
	}

	@Override
	public void updateMarker(MarkerInfo markerInfo, boolean isDraggable) {
		// if the drone hasn't received a gps signal yet
		final LatLong coord = markerInfo.getPosition();
		if (coord == null) {
			return;
		}

		final LatLng position = DroneHelper.CoordToLatLng(coord);
		Marker marker = mBiMarkersMap.getValue(markerInfo);
		if (marker == null) {
			marker = new Marker(mMapView, markerInfo.getTitle(), markerInfo.getSnippet(), position);
			mMarkersOverlay.addItem(marker);
			mBiMarkersMap.put(markerInfo, marker);
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
		final List<LatLong> pathCoords = pathSource.getPathPoints();

		if (mMissionPath == null) {
			mMissionPath = new PathOverlay(MISSION_PATH_DEFAULT_COLOR, MISSION_PATH_DEFAULT_WIDTH);
			mMapView.getOverlays().add(mMissionPath);
		}

		mMissionPath.clearPath();
		for (LatLong coord : pathCoords) {
			mMissionPath.addPoint(DroneHelper.CoordToLatLng(coord));
		}
	}

    @Override
    public void updatePolygonsPaths(List<List<LatLong>> paths) {

    }

    @Override
	public void zoomToFit(List<LatLong> coords) {
		if (coords.isEmpty()) {
			return;
		}

		final ArrayList<LatLng> boxCoords = new ArrayList<LatLng>(coords.size());
		for (LatLong coord : coords) {
			boxCoords.add(DroneHelper.CoordToLatLng(coord));
		}

		final BoundingBox enclosingBounds = BoundingBox.fromLatLngs(boxCoords);
		mMapView.zoomToBoundingBox(enclosingBounds, true, true, true);
	}

    @Override
    public void zoomToFitMyLocation(List<LatLong> coords) {
        if(mLocationProvider != null){
            LatLng userLocation = mLocationProvider.getMyLocation();
            if(userLocation != null){
                final List<LatLong> updatedCoords = new ArrayList<LatLong>(coords);
                updatedCoords.add(DroneHelper.ILatLngToCoord(userLocation));
                zoomToFit(updatedCoords);
                return;
            }
        }

        zoomToFit(coords);
    }

	@Override
	public void skipMarkerClickEvents(boolean skip) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void updateRealTimeFootprint(FootPrint footprint) {
        //TODO To be completed
    }
}
