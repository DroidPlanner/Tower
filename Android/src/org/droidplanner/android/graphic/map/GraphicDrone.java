package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicDrone implements OnDroneListener {

	private Marker droneMarker;
	private GoogleMap map;

	public GraphicDrone(Drone drone, GoogleMap map) {
		this.map = map;
		addMarkerToMap();
		drone.events.addDroneListener(this);
	}

	public void updatePosition(float yaw, Coord2D coord2d) {
		droneMarker.setPosition(DroneHelper.CoordToLatLang(coord2d));
		droneMarker.setRotation(yaw);
		droneMarker.setVisible(true);
	}

	private void addMarkerToMap() {
		droneMarker = map.addMarker(new MarkerOptions()
				.anchor((float) 0.5, (float) 0.5).position(new LatLng(0, 0))
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.quad))
				.visible(false).flat(true));
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case GPS:
			updatePosition((float) drone.orientation.getYaw(),
					drone.GPS.getPosition());
			break;
		default:
			break;
		}

	}
}
