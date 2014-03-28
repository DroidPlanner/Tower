package org.droidplanner.android.fragments.helpers;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.graphic.managers.MapManager;
import org.droidplanner.core.drone.Drone;

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

public abstract class DroneMap extends OfflineMapFragment {
	public MapManager manager;
	public Drone drone;
	
	protected Context context;
	public GoogleMap mMap;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup,
			Bundle bundle) {
		View view = super.onCreateView(inflater, viewGroup, bundle);
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		mMap = getMap();
		manager = new MapManager(getMap(),drone, getResources(),context);
		drone.events.addDroneListener(manager);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		loadCameraPosition();
		manager.update();
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(manager);
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

	
	public LatLng getMyLocation() {
		if (mMap.getMyLocation() != null) {
			return new LatLng(mMap.getMyLocation().getLatitude(), mMap
					.getMyLocation().getLongitude());
		} else {
			return null;
		}
	}

	/**
	 * @deprecated Use {@link org.droidplanner.android.graphic.managers.MapManager#update(org.droidplanner.android.fragments.helpers.DroneMap)} instead
	 */
	public void update() {
		manager.update();
	}
}
