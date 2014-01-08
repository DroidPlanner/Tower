package org.droidplanner.fragments.markers;

import org.droidplanner.R;
import org.droidplanner.fragments.FlightMapFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DroneMarker {
	private static final int ZOOM_LEVEL = 20;

	private Marker droneMarker;
	private FlightMapFragment flightMapFragment;

	public DroneMarker(FlightMapFragment flightMapFragment) {
		this.flightMapFragment = flightMapFragment;
		updateDroneMarkers();
	}

	private void updatePosition(float yaw, LatLng coord) {
		try {
			droneMarker.setPosition(coord);
			droneMarker.setRotation(yaw);
			droneMarker.setVisible(true);

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
		addMarkerToMap();
	}

	private void addMarkerToMap() {
		droneMarker = flightMapFragment.mMap.addMarker(new MarkerOptions()
				.anchor((float) 0.5, (float) 0.5).position(new LatLng(0, 0))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.quad)).visible(false)
				.flat(true));
	}

	public void onDroneUpdate() {
		updatePosition((float)flightMapFragment.drone.orientation.getYaw(),
				flightMapFragment.drone.GPS.getPosition());
		flightMapFragment.addFlightPathPoint(flightMapFragment.drone.GPS
				.getPosition());
	}
}