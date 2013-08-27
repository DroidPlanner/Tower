package com.droidplanner.fragments.markers;

import android.os.Handler;
import android.os.SystemClock;

import com.droidplanner.drone.DroneInterfaces.MapUpdatedListner;
import com.droidplanner.fragments.FlightMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DroneMarker implements MapUpdatedListner {
	private Marker droneMarker;
	private FlightMapFragment flightMapFragment;
	private DroneBitmaps bitmaps;
	private DroneInterpolator interpolator;

	public DroneMarker(FlightMapFragment flightMapFragment) {
		this.flightMapFragment = flightMapFragment;
		updateDroneMarkers();
		interpolator = new DroneInterpolator();
		interpolator.run();
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
					.newLatLngZoom(coord, 16));
		}
		if (flightMapFragment.isAutoPanEnabled) {
			flightMapFragment.mMap.animateCamera(CameraUpdateFactory
					.newLatLngZoom(droneMarker.getPosition(), 17));
		}
	}

	public void updateDroneMarkers() {
		if (droneMarker != null) {
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
		/*
		 * updatePosition(flightMapFragment.drone.orientation.getYaw(),
		 * flightMapFragment.drone.GPS.getPosition());
		 * flightMapFragment.addFlithPathPoint(flightMapFragment.drone.GPS
		 * .getPosition());
		 */
		interpolator.newPosition(flightMapFragment.drone.GPS.getPosition());
	}

	class DroneInterpolator {
		private static final int PERIOD_MS = 16;
		double velX = 0, velY = 0;
		LatLng lastPosition = new LatLng(0, 0);
		int iteration = 0;
		private long lastTime;

		public void newPosition(LatLng newPos) {
			long currentTime = SystemClock.elapsedRealtime();
			long dt = (currentTime-lastTime);
			lastTime = currentTime;
			
			velX = (newPos.longitude - lastPosition.longitude)/dt;
			velY = (newPos.latitude - lastPosition.latitude)/dt;
			lastPosition = newPos;
			
			iteration = 0;
		}
		
		public void run() {
			final Handler handler = new Handler();
			handler.post(new Runnable() {
				@Override
				public void run() {
					double yaw = flightMapFragment.drone.orientation.getYaw();
					double t = iteration * PERIOD_MS;
					LatLng coord = new LatLng(lastPosition.latitude + velY*t, lastPosition.longitude + velX*t);
					updatePosition(yaw, coord);

					iteration++;
					// Post again 16ms later.
					handler.postDelayed(this, PERIOD_MS);
				}
			});
		}

	}
}