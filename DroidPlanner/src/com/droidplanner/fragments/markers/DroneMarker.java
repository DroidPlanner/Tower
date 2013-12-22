package com.droidplanner.fragments.markers;

import com.droidplanner.fragments.FlightMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DroneMarker {
	private static final int ZOOM_LEVEL = 20;

	private Marker droneMarker;
	private FlightMapFragment flightMapFragment;
	private DroneBitmaps bitmaps;

	public DroneMarker(FlightMapFragment flightMapFragment) {
		this.flightMapFragment = flightMapFragment;
		updateDroneMarkers();
	}

	private void updatePosition(double yaw, LatLng coord) {
		// This ensure the 0 to 360 range
		double correctHeading = (yaw - flightMapFragment.getMapRotation() + 360) % 360;
		try {
			droneMarker.setVisible(true);
			droneMarker.setPosition(coord);
			droneMarker.setIcon(bitmaps.getIcon(correctHeading));

			animateCamera(coord);
		} catch (Exception e) {
		}
	}

	private void animateCamera(LatLng coord) {
		if (!flightMapFragment.hasBeenZoomed) {
			flightMapFragment.hasBeenZoomed = true;
			flightMapFragment.mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(coord, ZOOM_LEVEL));
		}
		if (flightMapFragment.isAutoPanEnabled) {
			flightMapFragment.mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(droneMarker.getPosition(), ZOOM_LEVEL));
		}
	}

	public void updateDroneMarkers() {
		if (droneMarker!=null) {
			droneMarker.remove();			
		}
		buildBitmaps();
		addMarkerToMap();
	}

	private void addMarkerToMap() {
		droneMarker = flightMapFragment.mMap.addMarker(new MarkerOptions()
				.anchor((float) 0.5, (float) 0.5).position(new LatLng(0, 0))
				.icon(bitmaps.getIcon(0)).visible(false));
	}

	private void buildBitmaps() {
		bitmaps = new DroneBitmaps(flightMapFragment.getResources(),
				flightMapFragment.drone.type.getType());
	}

	public void onDroneUpdate() {
		updatePosition(flightMapFragment.drone.orientation.getYaw(),
				flightMapFragment.drone.GPS.getPosition());
		flightMapFragment.addFlightPathPoint(flightMapFragment.drone.GPS
				.getPosition());
	}
}