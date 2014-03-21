package org.droidplanner.extra;

import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.fragments.FlightMapFragment;
import org.droidplanner.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicDrone implements OnDroneListener {

	private Marker droneMarker;
	private FlightMapFragment flightMapFragment;
	private Drone drone;

	public GraphicDrone(FlightMapFragment flightMapFragment) {
		this.flightMapFragment = flightMapFragment;
		this.drone = flightMapFragment.drone;
		addMarkerToMap();
		drone.events.addDroneListener(this);
	}

	private void updatePosition(float yaw, Coord2D coord2d) {
			droneMarker.setPosition(DroneHelper.CoordToLatLang(coord2d));
			droneMarker.setRotation(yaw);
			droneMarker.setVisible(true);
	}

	private void addMarkerToMap() {
		droneMarker = flightMapFragment.mMap.addMarker(new MarkerOptions()
				.anchor((float) 0.5, (float) 0.5).position(new LatLng(0, 0))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.quad)).visible(false)
				.flat(true));
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case GPS:
			updatePosition((float)flightMapFragment.drone.orientation.getYaw(),
					flightMapFragment.drone.GPS.getPosition());
			break;
		default:
			break;
		}

	}
}
