package org.droidplanner.android.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class DroneMap extends Fragment implements OnDroneListener {

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

		final Activity activity = getActivity();
		final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
		drone = app.getDrone();
		missionProxy = app.missionProxy;

		home = new GraphicHome(drone);
		graphicDrone = new GraphicDrone(drone);
		guided = new GraphicGuided(drone);

		updateMapFragment();

		return view;
	}

	private void updateMapFragment() {
		// Add the map fragment instance (based on user preference)
		final DPMapProvider mapProvider = Utils.getMapProvider(getActivity()
				.getApplicationContext());

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
		drone.removeDroneListener(this);
		mMapFragment.saveCameraPosition();
	}

	@Override
	public void onResume() {
		super.onResume();
		drone.addDroneListener(this);
		mMapFragment.loadCameraPosition();
		update();
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
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_UPDATE:
			update();
			break;

		case GPS_INTERPOLATED:
		case GPS:
			mMapFragment.updateMarker(graphicDrone);
			mMapFragment.updateDroneLeashPath(guided);
			if(drone.getGps().isPositionValid()) {
				mMapFragment.addFlightPathPoint(drone.getGps().getInterpolatedPosition());
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

	public void update() {
		mMapFragment.cleanMarkers();

		if (home.isValid()) {
			mMapFragment.updateMarker(home);
		}

		mMapFragment.updateMarkers(missionProxy.getMarkersInfos(), isMissionDraggable());
		mMapFragment.updateMissionPath(missionProxy);
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

}
