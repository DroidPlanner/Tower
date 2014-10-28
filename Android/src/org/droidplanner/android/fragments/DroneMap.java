package org.droidplanner.android.fragments;

import java.util.List;
import java.util.Set;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
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
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class DroneMap extends ApiListenerFragment implements OnDroneListener {

	private final static String TAG = DroneMap.class.getSimpleName();

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
    public void onApiConnected(DroidPlannerApi api){
            api.addDroneListener(this);

            drone = api.getDrone();
            missionProxy = api.getMissionProxy();

            home = new GraphicHome(drone);
            graphicDrone = new GraphicDrone(drone);
            guided = new GraphicGuided(drone);

            postUpdate();
    }

    @Override
    public void onApiDisconnected(){
        getApi().removeDroneListener(this);
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

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_UPDATE:
			postUpdate();
			break;

		case GPS:
			mMapFragment.updateMarker(graphicDrone);
			mMapFragment.updateDroneLeashPath(guided);
			if (drone.getGps().isPositionValid()) {
				mMapFragment.addFlightPathPoint(drone.getGps().getPosition());
			}
			break;

		case GUIDEDPOINT:
			mMapFragment.updateMarker(guided);
			mMapFragment.updateDroneLeashPath(guided);
			break;

		case HEARTBEAT_RESTORED:
		case HEARTBEAT_FIRST:
			mMapFragment.updateMarker(graphicDrone);
			break;

		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
			mMapFragment.updateMarker(graphicDrone);
			break;
		default:
			break;
		}
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

	public List<Coord2D> projectPathIntoMap(List<Coord2D> path) {
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
