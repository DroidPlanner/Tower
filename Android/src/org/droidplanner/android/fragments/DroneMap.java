package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class DroneMap extends Fragment implements OnDroneListener {

    protected static final float ZOOM_LEVEL = 20f;

    protected boolean hasBeenZoomed = false;

    protected DPMap mMapFragment;

    private GraphicHome home;
    public GraphicDrone graphicDrone;
    public GraphicGuided guided;

    protected MissionProxy missionProxy;
    public Drone drone;

    protected Context context;

	protected boolean isMissionDraggable(){
        return false;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        final View view = inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);

        final Activity activity = getActivity();
        final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
		drone = app.drone;
        missionProxy = app.missionProxy;

		home = new GraphicHome(drone);
        graphicDrone = new GraphicDrone(drone);
        guided = new GraphicGuided(drone);

        updateMapFragment();

		return view;
	}

    private void updateMapFragment(){
        //Add the map fragment instance (based on user preference)
        final DPMapProvider mapProvider = getMapProvider();

        final FragmentManager fm  = getChildFragmentManager();
        mMapFragment = (DPMap)fm.findFragmentById(R.id.map_fragment_container);
        if(mMapFragment == null || mMapFragment.getProvider() != mapProvider){
            final Bundle mapArgs = new Bundle();
            mapArgs.putInt(DPMap.EXTRA_MAX_FLIGHT_PATH_SIZE, getMaxFlightPathSize());

            mMapFragment = mapProvider.getMapFragment();
            ((Fragment)mMapFragment).setArguments(mapArgs);
            fm.beginTransaction().replace(R.id.map_fragment_container, (Fragment)mMapFragment)
                    .commit();
        }
    }

    /**
     * Returns the map provider to use in this fragment. This method exists so that children can
     * override the returned map provider based on their need.
     * @return map provider
     */
    protected DPMapProvider getMapProvider(){
        return Utils.getMapProvider(getActivity().getApplicationContext());
    }

    @Override
    public void onPause(){
     super.onPause();
        drone.events.removeDroneListener(this);
        mMapFragment.saveCameraPosition();
    }

    @Override
    public void onResume(){
        super.onResume();
        drone.events.addDroneListener(this);
        mMapFragment.loadCameraPosition();
        update();
    }

	@Override
	public void onStart() {
		super.onStart();
        updateMapFragment();
	}

	@Override
	public void onStop() {
		super.onStop();
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

            case GPS:
                mMapFragment.updateMarker(graphicDrone);
                mMapFragment.updateDroneLeashPath(guided);

                Coord2D dronePosition = drone.GPS.getPosition();
                mMapFragment.addFlightPathPoint(dronePosition);

                if(isAutoPanEnabled()){
                    float zoomLevel = hasBeenZoomed ? mMapFragment.getMapZoomLevel() : ZOOM_LEVEL;
                    hasBeenZoomed = true;
                    mMapFragment.updateCamera(dronePosition, zoomLevel);
                }
                break;

            case GUIDEDPOINT:
                mMapFragment.updateMarker(guided);
                mMapFragment.updateDroneLeashPath(guided);
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

    protected int getMaxFlightPathSize(){
        return 0;
    }

    protected boolean isAutoPanEnabled(){
        return false;
    }

    /**
     * Adds padding around the edges of the map.
     * @param left the number of pixels of padding to be added on the left of the map.
     * @param top the number of pixels of padding to be added on the top of the map.
     * @param right the number of pixels of padding to be added on the right of the map.
     * @param bottom the number of pixels of padding to be added on the bottom of the map.
     */
    public void setMapPadding(int left, int top, int right, int bottom){
        mMapFragment.setMapPadding(left, top, right, bottom);
    }

    public void saveCameraPosition(){
        mMapFragment.saveCameraPosition();
    }

    public List<Coord2D> projectPathIntoMap(List<Coord2D> path) {
        return mMapFragment.projectPathIntoMap(path);
    }
}
