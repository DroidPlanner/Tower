package com.droidplanner.fragments.markers;

import java.util.Locale;

import com.droidplanner.R.drawable;
import com.droidplanner.drone.Drone;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeMarker {
	public Marker homeMarker;
	private GoogleMap myMap;

	public HomeMarker(GoogleMap myMap) {
		this.myMap = myMap;
	}

	public void update(Drone drone) {
		if (homeMarker == null) {
			addMarkerToMap(drone);
		} else {
			updateMarker(drone);
		}
	}

	private void updateMarker(Drone drone) {
		homeMarker.setPosition(drone.mission.getHome().coord);
		homeMarker.setSnippet(String.format(Locale.ENGLISH, "%.2f",
				drone.mission.getHome().Height));
	}

	private void addMarkerToMap(Drone drone) {
		homeMarker = myMap.addMarker(new MarkerOptions()
				.position(drone.mission.getHome().coord)
				.snippet(
						String.format(Locale.ENGLISH, "%.2f",
								drone.mission.getHome().Height))
				.draggable(true)
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(drawable.ic_menu_home)).title("Home"));
	}
}