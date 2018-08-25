package co.aerobotics.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.DJIFlightControllerState;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.graphic.map.CameraMarker;
import co.aerobotics.android.graphic.map.GraphicHome;
import co.aerobotics.android.graphic.map.PolygonData;
import co.aerobotics.android.maps.MarkerInfo;
import co.aerobotics.android.media.ImageImpl;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import co.aerobotics.android.proxy.mission.item.markers.LastWaypointMarkerInfo;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;
import com.google.android.gms.maps.model.PolygonOptions;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.CameraProxy;
import com.o3dr.services.android.lib.drone.property.Gps;

import co.aerobotics.android.R;
import co.aerobotics.android.fragments.helpers.ApiListenerFragment;
import co.aerobotics.android.graphic.map.GraphicDrone;
import co.aerobotics.android.graphic.map.GraphicGuided;
import co.aerobotics.android.maps.DPMap;

import co.aerobotics.android.maps.PolylineInfo;
import co.aerobotics.android.maps.providers.DPMapProvider;
import co.aerobotics.android.maps.providers.google_map.tiles.mapbox.offline.MapDownloader;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import co.aerobotics.android.utils.Utils;
import co.aerobotics.android.utils.prefs.AutoPanMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import dji.common.error.DJIError;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

public abstract class DroneMap extends ApiListenerFragment {
    private static final String TAG = "dronemap";
    private static final String EXTRA_DRONE_FLIGHT_PATH = "extra_drone_flight_path";
    public static final String ACTION_UPDATE_MAP = Utils.PACKAGE_NAME + ".action.UPDATE_MAP";
	public static final String ACTION_UPDATE_CAMERA_MARKERS = "update_camera_markers";
	private static final IntentFilter eventFilter = new IntentFilter();
	static {
		eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
		eventFilter.addAction(AttributeEvent.CAMERA_FOOTPRINTS_UPDATED);
		eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
		eventFilter.addAction(AttributeEvent.HOME_UPDATED);
        eventFilter.addAction(ACTION_UPDATE_MAP);
		eventFilter.addAction(DroidPlannerApp.FLAG_CONNECTION_CHANGE);
		eventFilter.addAction(DJIFlightControllerState.FLAG_CONTROLLER_STATE);
		eventFilter.addAction(AeroviewPolygons.ACTION_POLYGON_UPDATE);
		eventFilter.addAction(ImageImpl.IMAGE_CAPTURED);
		eventFilter.addAction(MissionProxy.MISSION_CLEARED);
		eventFilter.addAction(ACTION_UPDATE_CAMERA_MARKERS);

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

				case DroidPlannerApp.FLAG_CONNECTION_CHANGE:
					if (!isConnected()) {
						removeListeners();
						mMapFragment.removeMarker(graphicDrone);
						mMapFragment.removeMarker(home);
					} else {
						setInitialDronePosition();
						setInitialHomePosition();
						removeListeners();
						addListeners();
					}
					break;

                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    home.updateMarker(DroneMap.this);
                    onMissionUpdate();
					updateLastWaypoint();
					removeCameraMarkers();
					//updateCameraMarkers();
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

				case AeroviewPolygons.ACTION_POLYGON_UPDATE:
					onPolygonUpdate();
					break;

				case ImageImpl.IMAGE_CAPTURED:
					addCameraMarker();
					//updateCameraMarkers();
					break;

				case MissionProxy.MISSION_CLEARED:
					//removeCameraMarkers();
					break;

				case ACTION_UPDATE_CAMERA_MARKERS:
					updateCameraMarkers();
					break;

            }
		}
	};

    protected final LinkedList<LatLongAlt> flightPathPoints = new LinkedList<>();

    private final Map<MissionItemProxy, List<MarkerInfo>> missionMarkers = new HashMap<>();
	private final LinkedList<MarkerInfo> externalMarkersToAdd = new LinkedList<>();
    private final LinkedList<PolylineInfo> externalPolylinesToAdd = new LinkedList<>();
	private final List<MarkerInfo> cameraMarkerInfos = new ArrayList<>();

	protected DPMap mMapFragment;

	protected DroidPlannerPrefs mAppPrefs;

	private GraphicHome home;
	private GraphicDrone graphicDrone;
	private GraphicGuided guided;
	private LastWaypointMarkerInfo lastWaypointMarker;
	private AeroviewPolygons aeroviewPolygons;

	protected MissionProxy missionProxy;
	protected Drone drone;

	protected Context context;

    private long lastDroneUpdate = 0;
    private long lastHomeUpdate = 0;

	private Double aircraftLatitude;
	private Double aircraftLongitude;
	private Double homeLatitude;
	private Double homeLongitude;
	private Double aircraftYaw;
	private LatLong homePosition;
	private Boolean isHomeLocationSet;

	private FlightControllerKey aircraftLatitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LATITUDE);
	private FlightControllerKey aircraftLongitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LONGITUDE);
	private FlightControllerKey aircraftYawKey = FlightControllerKey.create(FlightControllerKey.ATTITUDE_YAW);
	private FlightControllerKey homeLatitudeKey = FlightControllerKey.create(FlightControllerKey.HOME_LOCATION_LATITUDE);
	private FlightControllerKey homeLongitudeKey = FlightControllerKey.create(FlightControllerKey.HOME_LOCATION_LONGITUDE);
	private FlightControllerKey homeLocationKey = FlightControllerKey.create(FlightControllerKey.HOME_LOCATION);
	private FlightControllerKey isHomeLocationSetKey = FlightControllerKey.create(FlightControllerKey.IS_HOME_LOCATION_SET);

	KeyListener aircraftLatitudeListener = new KeyListener() {
		@Override
		public void onValueChange(Object o, Object o1) {
			if (o1 instanceof Double) {
				aircraftLatitude = (Double) o1;
				updateDroneMarker();
			}
		}
	};

	KeyListener aircraftLongitudeListener = new KeyListener() {
		@Override
		public void onValueChange(Object o, Object o1) {
			if (o1 instanceof Double) {
				aircraftLongitude = (Double) o1;
				updateDroneMarker();
			}
		}
	};

	KeyListener aircraftYawListener = new KeyListener() {
		@Override
		public void onValueChange(Object o, Object o1) {
			if (o1 instanceof Double) {
				aircraftYaw = (Double) o1;
				updateDroneMarker();
			}
		}
	};

	KeyListener homeLatitudeListener = new KeyListener() {
		@Override
		public void onValueChange(Object o, Object o1) {
			if (o1 instanceof Double) {
				homeLatitude = (Double) o1;
				updateHomeMarker();
			}
		}
	};

	KeyListener homeLongitudeListener = new KeyListener() {
		@Override
		public void onValueChange(Object o, Object o1) {
			if (o1 instanceof Double) {
				homeLongitude = (Double) o1;
				updateHomeMarker();
			}
		}
	};


	KeyListener homeLocationSetListener = new KeyListener() {
		@Override
		public void onValueChange(Object o, Object o1) {
			if (o1 instanceof Boolean) {
				if ((Boolean) o1){
					getHomeLatLong();
				}
			}
		}
	};

	public abstract boolean isMissionDraggable();

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
		getBroadcastManager().registerReceiver(eventReceiver, eventFilter);

		if (mMapFragment != null)
			mMapFragment.clearAll();

		drone = getDrone();
		missionProxy = getMissionProxy();
		home = new GraphicHome();
		graphicDrone = new GraphicDrone();
		guided = new GraphicGuided(drone);
		mMapFragment.addMarker(home);
		mMapFragment.addMarker(graphicDrone);
		mMapFragment.addMarker(guided);

        for(LatLongAlt point : flightPathPoints) {
            mMapFragment.addFlightPathPoint(point);
        }

        if(aeroviewPolygons == null) {
			aeroviewPolygons = new AeroviewPolygons(getActivity());
			aeroviewPolygons.addPolygonsToMap();
		}

		onPolygonUpdate();
		onMissionUpdate();
		if (isConnected()){
			setInitialDronePosition();
			setInitialHomePosition();
			removeListeners();
			addListeners();
		}
	}

	private void addListeners() {
		KeyManager.getInstance().addListener(aircraftLatitudeKey, aircraftLatitudeListener);
		KeyManager.getInstance().addListener(aircraftLongitudeKey, aircraftLongitudeListener);
		KeyManager.getInstance().addListener(aircraftYawKey, aircraftYawListener);
		KeyManager.getInstance().addListener(isHomeLocationSetKey, homeLocationSetListener);
		KeyManager.getInstance().addListener(homeLatitudeKey, homeLatitudeListener);
		KeyManager.getInstance().addListener(homeLocationKey, homeLongitudeListener);
	}

	private void removeListeners() {
		if (KeyManager.getInstance() != null) {
			KeyManager.getInstance().removeListener(homeLocationSetListener);
			KeyManager.getInstance().removeListener(aircraftLatitudeListener);
			KeyManager.getInstance().removeListener(aircraftLongitudeListener);
			KeyManager.getInstance().removeListener(aircraftYawListener);
			KeyManager.getInstance().removeListener(homeLongitudeListener);
			KeyManager.getInstance().removeListener(homeLatitudeListener);
		}
	}

	private void setInitialDronePosition() {
		KeyManager.getInstance().getValue(aircraftLongitudeKey, new GetCallback() {
			@Override
			public void onSuccess(Object o) {
				if (o instanceof Double) {
					aircraftLongitude = (double) o;
					updateDroneMarker();
				}
			}

			@Override
			public void onFailure(DJIError djiError) {
				Log.d(TAG, djiError.getDescription());

			}
		});

		KeyManager.getInstance().getValue(aircraftLatitudeKey, new GetCallback() {
			@Override
			public void onSuccess(Object o) {
				if (o instanceof Double) {
					aircraftLatitude = (double) o;
					updateDroneMarker();
				}
			}

			@Override
			public void onFailure(DJIError djiError) {
				Log.d(TAG, djiError.getDescription());

			}
		});

		KeyManager.getInstance().getValue(aircraftYawKey, new GetCallback() {
			@Override
			public void onSuccess(Object o) {
				if (o instanceof Double) {
					aircraftYaw = (double) o;
					updateDroneMarker();
				}
			}

			@Override
			public void onFailure(DJIError djiError) {

			}
		});
	}

	private void setInitialHomePosition() {
		KeyManager.getInstance().getValue(isHomeLocationSetKey, new GetCallback() {
			@Override
			public void onSuccess(Object o) {
				if (o instanceof Boolean) {
					if ((Boolean) o) {
						getHomeLatLong();
					}
				}
			}

			@Override
			public void onFailure(DJIError djiError) {

			}
		});
	}


	private void getHomeLatLong() {
		KeyManager.getInstance().getValue(homeLatitudeKey, new GetCallback() {
			@Override
			public void onSuccess(Object o) {
				if (o instanceof Double) {
					homeLatitude = (Double) o;
					updateHomeMarker();
				}
			}

			@Override
			public void onFailure(DJIError djiError) {

			}
		});

		KeyManager.getInstance().getValue(homeLongitudeKey, new GetCallback() {
			@Override
			public void onSuccess(Object o) {
				if (o instanceof Double) {
					homeLongitude = (Double) o;
					updateHomeMarker();
				}
			}

			@Override
			public void onFailure(DJIError djiError) {

			}
		});
	}


	protected synchronized void updateDroneMarker() {
		Log.i(TAG, "update drone marker");
		if (aircraftLatitude != null && aircraftLongitude != null && aircraftYaw != null) {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					graphicDrone.setGraphicDroneLon(aircraftLongitude);
					graphicDrone.setGraphicDroneLat(aircraftLatitude);
					graphicDrone.setYaw(aircraftYaw);
					graphicDrone.updateMarker(DroneMap.this);
					lastDroneUpdate = System.currentTimeMillis();
				}
			});
		}
	}

    private synchronized void updateHomeMarker(){
		if (homeLatitude != null && homeLongitude != null && checkGpsCoordination(homeLatitude, homeLongitude)) {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					home.setHomePosition(homeLatitude, homeLongitude);
					home.updateMarker(DroneMap.this);
				}
			}, 100);
		}
    }

    private void updateLastWaypoint(){
		List<MissionItemProxy> proxyMissionItems = missionProxy.getItems();
		if (proxyMissionItems.size() > 0) {
			for (MissionItemProxy proxyItem : proxyMissionItems) {
				if (proxyItem.getMissionItem() instanceof Survey) {
					Survey survey = (Survey) proxyItem.getMissionItem();
					List<LatLong> gridPoints = survey.getGridPoints();
					if (gridPoints != null && gridPoints.size() != 0) {
						if (lastWaypointMarker == null) {
							lastWaypointMarker = new LastWaypointMarkerInfo(gridPoints.get(gridPoints.size() - 1));
						} else {
							lastWaypointMarker.setPosition(gridPoints.get(gridPoints.size() - 1));
						}
					}
				}

				if (lastWaypointMarker != null) {
					lastWaypointMarker.updateMarker(DroneMap.this);
				}
			}
		} else {
			if (lastWaypointMarker != null) {
				lastWaypointMarker.removeProxyMarker();
			}
		}
	}

	private boolean isConnected(){
		BaseProduct product = DJISDKManager.getInstance().getProduct();
        return product != null && product.isConnected();
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
			refreshMarkers(proxyMarkers);
		}

		// Remove the now invalid mission items
		for(List<MarkerInfo> invalidMarkers : missionMarkers.values()) {
			mMapFragment.removeMarkers(invalidMarkers);
		}
		missionMarkers.clear();

        missionMarkers.putAll(newMissionMarkers);
    }


	private void onPolygonUpdate(){
        mMapFragment.clearPolygons();
		Map<String, PolygonData> map = DroidPlannerApp.getInstance().polygonMap;
		if (map != null){
			for (Map.Entry<String, PolygonData> entry : map.entrySet()){
				PolygonOptions polygonOptions = new PolygonOptions().addAll(entry.getValue().getPoints()).strokeColor(getResources().getColor(R.color.primary_light_blue)).strokeWidth(4).fillColor(entry.getValue().getColour()).clickable(true);
				mMapFragment.addPolygon(polygonOptions, entry.getValue().getId());
			}
		}
	}

    private void refreshMarkers(List<MarkerInfo> proxyMarkers){
		for(MarkerInfo marker: proxyMarkers){
			marker.updateMarker(DroneMap.this);
		}
	}

/*
	private void updateCameraMarkers(){
		List<MissionItemProxy> proxyMissionItems = missionProxy.getItems();
		if (!proxyMissionItems.isEmpty()) {
			for (Map.Entry<LatLng, Float> item : DroidPlannerApp.getInstance().cameraMarkerInfoMap.entrySet()) {
				MarkerInfo cameraPosition = new CameraMarker(item.getKey(), item.getValue());
				cameraMarkerInfos.add(cameraPosition);
				cameraPosition.updateMarker(DroneMap.this);
			}
		} else {
			removeCameraMarkers();
		}
	}
*/

	private void addCameraMarker(){
		List<CameraMarker> cameraMarkers = DroidPlannerApp.getInstance().cameraMarkers;
		if(!cameraMarkers.isEmpty()) {
			CameraMarker latest = cameraMarkers.get(cameraMarkers.size() - 1);
			mMapFragment.addMarker(latest);
		}
	}

	private void updateCameraMarkers(){
		List<CameraMarker> cameraMarkers = DroidPlannerApp.getInstance().cameraMarkers;

		Collection<MarkerInfo> markerInfos = new ArrayList<MarkerInfo>(DroidPlannerApp.getInstance().cameraMarkers);
		mMapFragment.removeMarkers(markerInfos);
		for (CameraMarker cameraMarker: cameraMarkers){
			mMapFragment.addMarker(cameraMarker);
		}

	}

	private void removeCameraMarkers(){
/*		if (mMapFragment != null){
			for (MarkerInfo position : cameraMarkerInfos) {
				position.removeProxyMarker();
			}
			cameraMarkerInfos.clear();
			DroidPlannerApp.getInstance().cameraMarkerInfoMap.clear();
		}
		*/
		if(missionProxy.getItems().isEmpty()) {
			List<CameraMarker> cameraMarkers = DroidPlannerApp.getInstance().cameraMarkers;
			if (!cameraMarkers.isEmpty()) {
				for (CameraMarker cameraMarker : cameraMarkers) {
					mMapFragment.removeMarker(cameraMarker);
				}
			}

			DroidPlannerApp.getInstance().cameraMarkers.clear();
		}
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

	private static boolean checkGpsCoordination(double latitude, double longitude) {
		if (!Double.isNaN(latitude) && !Double.isNaN(longitude)){
			return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
		}
		else{
			return false;
		}

	}
}
