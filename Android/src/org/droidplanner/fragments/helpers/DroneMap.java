package org.droidplanner.fragments.helpers;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.graphic.GraphicHome;
import org.droidplanner.graphic.GraphicMission;
import org.droidplanner.graphic.markers.MarkerManager;

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

public abstract class DroneMap extends OfflineMapFragment implements OnDroneListener {
	public GoogleMap mMap;
	protected MarkerManager markers;
	protected MapPath missionPath;
	public Drone drone;
	public GraphicMission mission;
	protected Context context;
	private GraphicHome home;
	
	protected abstract boolean isMissionDraggable();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		home = new GraphicHome(drone);
		mission = new GraphicMission(drone);
		mMap = getMap();
		markers = new MarkerManager(mMap);
		missionPath = new MapPath(mMap,getResources());
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
		switch (event) {
		case MISSION_UPDATE:
			update();
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

		markers.updateMarkers(mission.getMarkers(), isMissionDraggable(), context);

		missionPath.update(mission);
	}

}
