package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.CameraProxy;
import com.o3dr.services.android.lib.drone.property.Gps;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.PolylineInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class DroneMap extends ApiListenerFragment {

    private static final String EXTRA_DRONE_FLIGHT_PATH = "extra_drone_flight_path";
    public static final String ACTION_UPDATE_MAP = Utils.PACKAGE_NAME + ".action.UPDATE_MAP";

	private static final IntentFilter eventFilter = new IntentFilter();
	static {
		eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
		eventFilter.addAction(AttributeEvent.GPS_POSITION);
		eventFilter.addAction(AttributeEvent.GUIDED_POINT_UPDATED);
		eventFilter.addAction(AttributeEvent.HEARTBEAT_FIRST);
		eventFilter.addAction(AttributeEvent.HEARTBEAT_RESTORED);
		eventFilter.addAction(AttributeEvent.HEARTBEAT_TIMEOUT);
		eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
		eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
		eventFilter.addAction(AttributeEvent.CAMERA_FOOTPRINTS_UPDATED);
		eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
		eventFilter.addAction(AttributeEvent.HOME_UPDATED);
        eventFilter.addAction(ACTION_UPDATE_MAP);
	}

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isResumed())
				return;

			final String action = intent.getAction();
            switch (action) {
                case ACTION_UPDATE_MAP:
					guided.updateMarker(DroneMap.this);
					break;

				case AttributeEvent.HOME_UPDATED:
					home.updateMarker(DroneMap.this);
					break;

                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
					home.updateMarker(DroneMap.this);
					onMissionUpdate();
                    break;

                case AttributeEvent.GPS_POSITION: {
					graphicDrone.updateMarker(DroneMap.this);
                    mMapFragment.updateDroneLeashPath(guided);
                    updateFlightPath();
                    break;
                }

                case AttributeEvent.GUIDED_POINT_UPDATED:
					guided.updateMarker(DroneMap.this);
                    mMapFragment.updateDroneLeashPath(guided);
                    break;

                case AttributeEvent.HEARTBEAT_FIRST:
                case AttributeEvent.HEARTBEAT_RESTORED:
				case AttributeEvent.STATE_CONNECTED:
					graphicDrone.updateMarker(DroneMap.this);
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.HEARTBEAT_TIMEOUT:
					graphicDrone.updateMarker(DroneMap.this);
                    break;

                case AttributeEvent.CAMERA_FOOTPRINTS_UPDATED: {
					if(mAppPrefs.isRealtimeFootprintsEnabled()) {
						CameraProxy camera = drone.getAttribute(AttributeType.CAMERA);
						if (camera != null && camera.getLastFootPrint() != null)
							mMapFragment.addCameraFootprint(camera.getLastFootPrint());
					}
                    break;
                }

                case AttributeEvent.ATTITUDE_UPDATED: {
                    if (mAppPrefs.isRealtimeFootprintsEnabled()) {
                        final Gps droneGps = drone.getAttribute(AttributeType.GPS);
                        if (droneGps.isValid()) {
                            CameraProxy camera = drone.getAttribute(AttributeType.CAMERA);
                            if (camera != null && camera.getCurrentFieldOfView() != null)
                                mMapFragment.updateRealTimeFootprint(camera.getCurrentFieldOfView());
                        }

                    }
                    else{
                        mMapFragment.updateRealTimeFootprint(null);
                    }
                    break;
                }
            }
		}
	};

    protected final LinkedList<LatLongAlt> flightPathPoints = new LinkedList<>();

    private final Map<MissionItemProxy, List<MarkerInfo>> missionMarkers = new HashMap<>();
	private final LinkedList<MarkerInfo> externalMarkersToAdd = new LinkedList<>();
    private final LinkedList<PolylineInfo> externalPolylinesToAdd = new LinkedList<>();

	protected DPMap mMapFragment;

	protected DroidPlannerPrefs mAppPrefs;

	private GraphicHome home;
	private GraphicDrone graphicDrone;
	private GraphicGuided guided;

	protected MissionProxy missionProxy;
	protected Drone drone;

	protected Context context;

	protected abstract boolean isMissionDraggable();

	public DPMap getMapFragment(){
		return mMapFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		final View view = inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);
		updateMapFragment();

		if(bundle != null){
			flightPathPoints.clear();
			List<LatLongAlt> flightPoints = (List<LatLongAlt>) bundle.getSerializable(EXTRA_DRONE_FLIGHT_PATH);
			if(flightPoints != null && !flightPoints.isEmpty()){
                flightPathPoints.addAll(flightPoints);
			}
		}

		return view;
	}

	@Override
	public void onApiConnected() {
		if (mMapFragment != null)
			mMapFragment.clearAll();

		drone = getDrone();
		missionProxy = getMissionProxy();

		home = new GraphicHome(drone, getContext());
		mMapFragment.addMarker(home);

		graphicDrone = new GraphicDrone(drone);
		mMapFragment.addMarker(graphicDrone);

		guided = new GraphicGuided(drone);
		mMapFragment.addMarker(guided);

        for(LatLongAlt point : flightPathPoints) {
            mMapFragment.addFlightPathPoint(point);
        }

		onMissionUpdate();
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
	}

    private void updateFlightPath(){
        if(showFlightPath()) {
            LatLongAlt currentFlightPoint = getCurrentFlightPoint();
            if(currentFlightPoint != null){
                mMapFragment.addFlightPathPoint(currentFlightPoint);
                flightPathPoints.add(currentFlightPoint);
            }
        }
    }

    protected LatLongAlt getCurrentFlightPoint(){
        final Gps droneGps = drone.getAttribute(AttributeType.GPS);
        if (droneGps != null && droneGps.isValid()) {
            Altitude droneAltitude = drone.getAttribute(AttributeType.ALTITUDE);
            LatLongAlt point = new LatLongAlt(droneGps.getPosition(),
                droneAltitude.getAltitude());
            return point;
        }
        return null;
    }

    protected final void onMissionUpdate(){
		if (!shouldUpdateMission()) {
            return;
        }

        mMapFragment.updateMissionPath(missionProxy);

        mMapFragment.updatePolygonsPaths(missionProxy.getPolygonsPath());

        //TODO: improve mission markers rendering performance
		List<MissionItemProxy> proxyMissionItems = missionProxy.getItems();
        // Clear the previous proxy mission item markers.
		Map<MissionItemProxy, List<MarkerInfo>> newMissionMarkers = new HashMap<>(proxyMissionItems.size());

		for(MissionItemProxy proxyItem : proxyMissionItems){
			List<MarkerInfo> proxyMarkers = missionMarkers.remove(proxyItem);
			if(proxyMarkers == null){
				proxyMarkers = MissionItemMarkerInfo.newInstance(proxyItem);

				if(!proxyMarkers.isEmpty()){
					// Add the new markers to the map.
					mMapFragment.addMarkers(proxyMarkers, isMissionDraggable());
				}
			}
			else {
				//Refresh the proxy markers
				for(MarkerInfo marker: proxyMarkers){
                    if (marker.isOnMap()) {
                        marker.updateMarker(DroneMap.this);
                    } else {
                        mMapFragment.addMarker(marker);
                    }
                }
			}
            newMissionMarkers.put(proxyItem, proxyMarkers);
		}

		// Remove the now invalid mission items
		for(List<MarkerInfo> invalidMarkers : missionMarkers.values()) {
			mMapFragment.removeMarkers(invalidMarkers);
		}
		missionMarkers.clear();

        missionMarkers.putAll(newMissionMarkers);
    }

    protected boolean shouldUpdateMission() {
        return true;
    }

    public void downloadMapTiles(MapDownloader mapDownloader, int minimumZ, int maximumZ){
		if(mMapFragment == null)
			return;

		mMapFragment.downloadMapTiles(mapDownloader, getVisibleMapArea(), minimumZ, maximumZ);
	}

	@Override
	public void onApiDisconnected() {
		getBroadcastManager().unregisterReceiver(eventReceiver);
	}

	private void updateMapFragment() {
		// Add the map fragment instance (based on user preference)
		final DPMapProvider mapProvider = mAppPrefs.getMapProvider();

		final FragmentManager fm = getChildFragmentManager();
		mMapFragment = (DPMap) fm.findFragmentById(R.id.map_fragment_container);
		if (mMapFragment == null || mMapFragment.getProvider() != mapProvider) {
			final Bundle mapArgs = new Bundle();
			mapArgs.putBoolean(DPMap.EXTRA_SHOW_FLIGHT_PATH, showFlightPath());

			mMapFragment = mapProvider.getMapFragment();
			((Fragment) mMapFragment).setArguments(mapArgs);
			fm.beginTransaction().replace(R.id.map_fragment_container, (Fragment) mMapFragment)
					.commit();
		}

		if(!externalMarkersToAdd.isEmpty()){
			for(MarkerInfo markerInfo = externalMarkersToAdd.poll();
                markerInfo != null && !externalMarkersToAdd.isEmpty();
                markerInfo = externalMarkersToAdd.poll()){
				mMapFragment.addMarker(markerInfo);
			}
		}
        if (!externalPolylinesToAdd.isEmpty()) {
            for(PolylineInfo polylineInfo = externalPolylinesToAdd.poll();
                polylineInfo != null && !externalPolylinesToAdd.isEmpty();
                polylineInfo = externalPolylinesToAdd.poll()){
                mMapFragment.addPolyline(polylineInfo);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if(mMapFragment != null) {
            if(!flightPathPoints.isEmpty()){
                outState.putSerializable(EXTRA_DRONE_FLIGHT_PATH, flightPathPoints);
            }
        }
    }

	@Override
	public void onPause() {
		super.onPause();
		mMapFragment.saveCameraPosition();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapFragment.loadCameraPosition();
	}

	@Override
	public void onStart() {
		super.onStart();
		updateMapFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity.getApplicationContext();
		mAppPrefs = DroidPlannerPrefs.getInstance(context);
	}

	protected boolean showFlightPath(){
        return false;
    }

	protected void clearFlightPath(){
		if (mMapFragment != null) {
			mMapFragment.clearFlightPath();
		}
        flightPathPoints.clear();
	}

	/**
	 * Adds padding around the edges of the map.
	 * 
	 * @param left
	 *            the number of pixels of padding to be added on the left of the
	 *            map.
	 * @param top
	 *            the number of pixels of padding to be added on the top of the
	 *            map.
	 * @param right
	 *            the number of pixels of padding to be added on the right of
	 *            the map.
	 * @param bottom
	 *            the number of pixels of padding to be added on the bottom of
	 *            the map.
	 */
	public void setMapPadding(int left, int top, int right, int bottom) {
		mMapFragment.setMapPadding(left, top, right, bottom);
	}

	public void saveCameraPosition() {
		mMapFragment.saveCameraPosition();
	}

	public List<LatLong> projectPathIntoMap(List<LatLong> path) {
		return mMapFragment.projectPathIntoMap(path);
	}

	/**
	 * Set map panning mode on the specified target.
	 * 
	 * @param target
	 */
	public abstract boolean setAutoPanMode(AutoPanMode target);

	/**
	 * Move the map to the user location.
	 */
	public void goToMyLocation() {
		mMapFragment.goToMyLocation();
	}

	/**
	 * Move the map to the drone location.
	 */
	public void goToDroneLocation() {
		mMapFragment.goToDroneLocation();
	}

	/**
	 * Update the map rotation.
	 * 
	 * @param bearing
	 */
	public void updateMapBearing(float bearing) {
		mMapFragment.updateCameraBearing(bearing);
	}

	public void addMarker(MarkerInfo markerInfo){
		if(markerInfo == null)
			return;

		if(mMapFragment != null) {
			mMapFragment.addMarker(markerInfo);
		}
		else{
			externalMarkersToAdd.add(markerInfo);
		}
	}

    public void addPolyline(PolylineInfo polylineInfo) {
        if (polylineInfo == null)
            return;

        if(mMapFragment != null) {
            mMapFragment.addPolyline(polylineInfo);
        }
        else {
            externalPolylinesToAdd.add(polylineInfo);
        }
    }

	public void removeMarker(MarkerInfo markerInfo){
		if(markerInfo == null)
			return;

		if(mMapFragment != null){
			mMapFragment.removeMarker(markerInfo);
		}
		else{
			externalMarkersToAdd.remove(markerInfo);
		}
	}

    public void removePolyline(PolylineInfo polylineInfo) {
        if(polylineInfo == null)
            return;

        if(mMapFragment != null){
            mMapFragment.removePolyline(polylineInfo);
        }
        else{
            externalPolylinesToAdd.remove(polylineInfo);
        }
    }

	public DPMap.VisibleMapArea getVisibleMapArea(){
		return mMapFragment == null ? null : mMapFragment.getVisibleMapArea();
	}
}
