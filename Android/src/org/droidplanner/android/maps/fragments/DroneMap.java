package org.droidplanner.android.maps.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.mission.MissionRender;
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

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public abstract class DroneMap extends Fragment implements OnDroneListener {

    //TODO: replace with DPMap type when the interface is complete.
    protected GoogleMapFragment mMapFragment;

    private GraphicHome home;
    public GraphicDrone graphicDrone;
    public GraphicGuided guided;

    protected MissionRender missionRender;
    public Drone drone;

    protected Context context;

	protected abstract boolean isMissionDraggable();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        final View view = inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);

        final DroidPlannerApp app = ((DroidPlannerApp) getActivity().getApplication());
		drone = app.drone;
        missionRender = app.missionRender;

		home = new GraphicHome(drone);
        graphicDrone = new GraphicDrone(drone);
        guided = new GraphicGuided(drone);

        //Add the map fragment instance (based on user preference)
        FragmentManager fm  = getChildFragmentManager();
        mMapFragment = (GoogleMapFragment)fm.findFragmentById(R.id.map_fragment_container);
        if(mMapFragment == null){
            final Bundle mapArgs = new Bundle();
            mapArgs.putInt(GoogleMapFragment.EXTRA_MAX_FLIGHT_PATH_SIZE, getMaxFlightPathSize());

            mMapFragment = new GoogleMapFragment();
            mMapFragment.setArguments(mapArgs);
            fm.beginTransaction().add(R.id.map_fragment_container, mMapFragment).commit();
        }

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		mMapFragment.loadCameraPosition();
		update();
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
		mMapFragment.saveCameraPosition();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity.getApplicationContext();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
        final LatLng position = DroneHelper.CoordToLatLang(drone.GPS.getPosition());
        switch (event) {
            case MISSION_UPDATE:
                update();
                break;

            case GPS:
                mMapFragment.updateMarker(graphicDrone, false);
                mMapFragment.updateDroneLeashPath(guided);
                mMapFragment.addFlightPathPoint(position);
                break;

            case GUIDEDPOINT:
                mMapFragment.updateMarker(guided, true);
                mMapFragment.updateDroneLeashPath(guided);
                break;

            default:
                break;
        }
    }

	public void update() {
		mMapFragment.cleanMarkers();

		if (home.isValid()) {
			mMapFragment.updateMarker(home, false);
		}

        mMapFragment.updateMarkers(missionRender.getMarkers(), isMissionDraggable());
        mMapFragment.updateMissionPath(missionRender.getPathPoints());
	}

    protected int getMaxFlightPathSize(){
        return 0;
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
