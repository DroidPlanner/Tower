package org.droidplanner.android.fragments;

import java.util.List;
import java.util.Set;

import org.droidplanner.R;
import org.droidplanner.android.api.Drone;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.AutoPanMode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.Gps;

public abstract class DroneMap extends ApiListenerFragment {

	private final static String TAG = DroneMap.class.getSimpleName();

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(Event.EVENT_GPS);
        eventFilter.addAction(Event.EVENT_GUIDED_POINT);
        eventFilter.addAction(Event.EVENT_HEARTBEAT_FIRST);
        eventFilter.addAction(Event.EVENT_HEARTBEAT_RESTORED);
        eventFilter.addAction(Event.EVENT_HEARTBEAT_TIMEOUT);
        eventFilter.addAction(Event.EVENT_DISCONNECTED);
        eventFilter.addAction(Event.EVENT_FOOTPRINT);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isResumed())
                return;

            final String action = intent.getAction();
            if(MissionProxy.ACTION_MISSION_PROXY_UPDATE.equals(action)){
                postUpdate();
            }
            else if(Event.EVENT_GPS.equals(action)){
                mMapFragment.updateMarker(graphicDrone);
                mMapFragment.updateDroneLeashPath(guided);
                final Gps droneGps = drone.getGps();
                if (droneGps != null && droneGps.isValid()) {
                    mMapFragment.addFlightPathPoint(droneGps.getPosition());
                }
            }
            else if(Event.EVENT_GUIDED_POINT.equals(action)){
                mMapFragment.updateMarker(guided);
                mMapFragment.updateDroneLeashPath(guided);
            }
            else if(Event.EVENT_HEARTBEAT_FIRST.equals(action)
                    || Event.EVENT_HEARTBEAT_RESTORED.equals(action)){
                mMapFragment.updateMarker(graphicDrone);
            }
            else if(Event.EVENT_DISCONNECTED.equals(action)
                    || Event.EVENT_HEARTBEAT_TIMEOUT.equals(action)){
                mMapFragment.updateMarker(graphicDrone);
            }
            else if(Event.EVENT_FOOTPRINT.equals(action)) {
                mMapFragment.addCameraFootprint(drone.getLastCameraFootPrint());
            }
        }
    };

	private final Handler mHandler = new Handler();

	private final Runnable mUpdateMap = new Runnable() {
		@Override
		public void run() {
            if(getActivity() == null && mMapFragment == null)
                return;

			final List<MarkerInfo> missionMarkerInfos = missionProxy.getMarkersInfos();

			final boolean isThereMissionMarkers = !missionMarkerInfos.isEmpty();
			final boolean isHomeValid = home.isValid();
            final boolean isGuidedVisible = guided.isVisible();

			// Get the list of markers currently on the map.
			final Set<MarkerInfo> markersOnTheMap = mMapFragment.getMarkerInfoList();

			if (!markersOnTheMap.isEmpty()) {
				if (isHomeValid) {
					markersOnTheMap.remove(home);
				}

                if(isGuidedVisible){
                    markersOnTheMap.remove(guided);
                }

				if (isThereMissionMarkers) {
					markersOnTheMap.removeAll(missionMarkerInfos);
				}

				mMapFragment.removeMarkers(markersOnTheMap);
			}

			if (isHomeValid) {
				mMapFragment.updateMarker(home);
			}

            if(isGuidedVisible){
                mMapFragment.updateMarker(guided);
            }

			if (isThereMissionMarkers) {
				mMapFragment.updateMarkers(missionMarkerInfos, isMissionDraggable());
			}

			mMapFragment.updateMissionPath(missionProxy);
			
			mMapFragment.updatePolygonsPaths(missionProxy.getPolygonsPath());

			mHandler.removeCallbacks(this);
		}
	};

	protected DPMap mMapFragment;

	private GraphicHome home;
	public GraphicDrone graphicDrone;
	public GraphicGuided guided;

	protected MissionProxy missionProxy;
	public Drone drone;

	protected Context context;

	protected abstract boolean isMissionDraggable();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		final View view = inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);
        updateMapFragment();
        return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mHandler.removeCallbacksAndMessages(null);
	}

    @Override
    public void onApiConnected(){
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);

            drone = getDrone();
            missionProxy = getMissionProxy();

            home = new GraphicHome(drone);
            graphicDrone = new GraphicDrone(drone);
            guided = new GraphicGuided(drone);

            postUpdate();
    }

    @Override
    public void onApiDisconnected(){
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

	private void updateMapFragment() {
		// Add the map fragment instance (based on user preference)
		final DPMapProvider mapProvider = Utils.getMapProvider(context);

		final FragmentManager fm = getChildFragmentManager();
		mMapFragment = (DPMap) fm.findFragmentById(R.id.map_fragment_container);
		if (mMapFragment == null || mMapFragment.getProvider() != mapProvider) {
			final Bundle mapArgs = new Bundle();
			mapArgs.putInt(DPMap.EXTRA_MAX_FLIGHT_PATH_SIZE, getMaxFlightPathSize());

			mMapFragment = mapProvider.getMapFragment();
			((Fragment) mMapFragment).setArguments(mapArgs);
			fm.beginTransaction().replace(R.id.map_fragment_container, (Fragment) mMapFragment)
					.commit();
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
    public void onStop(){
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        context = activity.getApplicationContext();
    }

	public final void postUpdate() {
		mHandler.post(mUpdateMap);
	}

	protected int getMaxFlightPathSize() {
		return 0;
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
     * @param bearing
     */
    public void updateMapBearing(float bearing){
        mMapFragment.updateCameraBearing(bearing);
    }

    /**
     * Ignore marker clicks on the map and instead report the event as a mapClick
     * @param skip if it should skip further events
     */
    public void skipMarkerClickEvents(boolean skip){
    	mMapFragment.skipMarkerClickEvents(skip);
    }
}
