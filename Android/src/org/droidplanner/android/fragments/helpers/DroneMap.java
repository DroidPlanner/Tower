package org.droidplanner.android.fragments.helpers;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.mission.MissionRender;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.graphic.map.MarkerManager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public abstract class DroneMap extends OfflineMapFragment implements OnDroneListener {
	public GoogleMap mMap;

	protected MarkerManager markers;
    protected MapPath droneLeashPath;
    private Polyline flightPath;
    private GraphicHome home;
    public GraphicDrone droneMarker;
    public GraphicGuided guided;
    public int maxFlightPathSize;

    protected MissionRender missionRender;
    public Drone drone;

    protected Context context;

	protected abstract boolean isMissionDraggable();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
        final DroidPlannerApp app = ((DroidPlannerApp) getActivity().getApplication());
		drone = app.drone;
		home = new GraphicHome(drone);
		mMap = getMap();
		markers = new MarkerManager(mMap);

        missionRender = app.missionRender;
        droneMarker = new GraphicDrone(drone, mMap);
        droneLeashPath = new MapPath(mMap, getResources());
        guided = new GraphicGuided(drone);

        addFlightPathToMap();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		loadCameraPosition();
		update();
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
		saveCameraPosition();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity.getApplicationContext();
	}

    public void clearFlightPath() {
        List<LatLng> oldFlightPath = flightPath.getPoints();
        oldFlightPath.clear();
        flightPath.setPoints(oldFlightPath);
    }

    private void addFlightPathToMap() {
        PolylineOptions flightPathOptions = new PolylineOptions();
        flightPathOptions.color(0xfffd693f).width(6).zIndex(1);
        flightPath = mMap.addPolyline(flightPathOptions);
    }

    public void addFlightPathPoint(LatLng position) {
        if (maxFlightPathSize > 0) {
            List<LatLng> oldFlightPath = flightPath.getPoints();
            if (oldFlightPath.size() > maxFlightPathSize) {
                oldFlightPath.remove(0);
            }
            oldFlightPath.add(position);
            flightPath.setPoints(oldFlightPath);
        }
    }

	/**
	 * Save the map camera state on a preference file
	 * http://stackoverflow.com/questions/16697891/google-maps-android-api-v2-restoring-map-state/16698624#16698624
	 */
	public void saveCameraPosition() {
		CameraPosition camera = mMap.getCameraPosition();
		SharedPreferences settings = context.getSharedPreferences("MAP", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat("lat", (float) camera.target.latitude);
		editor.putFloat("lng", (float) camera.target.longitude);
		editor.putFloat("bea", camera.bearing);
		editor.putFloat("tilt", camera.tilt);
		editor.putFloat("zoom", camera.zoom);
		editor.commit();
	}

	private void loadCameraPosition() {
		Builder camera = new CameraPosition.Builder();
		SharedPreferences settings = context.getSharedPreferences("MAP", 0);
		camera.bearing(settings.getFloat("bea", 0));
		camera.tilt(settings.getFloat("tilt", 0));
		camera.zoom(settings.getFloat("zoom", 0));
		camera.target(new LatLng(settings.getFloat("lat", 0),settings.getFloat("lng", 0)));
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera.build()));
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
        final LatLng position = DroneHelper.CoordToLatLang(drone.GPS.getPosition());
		switch (event) {
		case MISSION_UPDATE:
			update();
			break;

            case GPS:
                droneLeashPath.update(guided);
                addFlightPathPoint(position);
                break;

            case GUIDEDPOINT:
                markers.updateMarker(guided, true, context);
                droneLeashPath.update(guided);
                break;

		default:
			break;
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

	public void update() {
		markers.clean();

		if (home.isValid()) {
			markers.updateMarker(home, false, context);
		}

		markers.updateMarkers(missionRender.getMarkers(), isMissionDraggable(), context);
        missionRender.updateMissionPath(mMap);
	}

}
