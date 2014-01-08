package org.droidplanner.fragments.markers;

import org.droidplanner.drone.variables.Home;

import org.droidplanner.R.drawable;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeMarker {
	public static MarkerOptions build(Home home) {
		return new MarkerOptions()
				.position(home.getCoord())
				.visible(home.isValid())
				.title("Home")
				.snippet(home.getAltitude().toString())
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(drawable.ic_menu_home)).title("Home");
	}

	public static void update(Marker marker, Home home) {
		marker.setVisible(home.isValid());
		marker.setPosition(home.getCoord());
		marker.setSnippet("Home "+ home.getAltitude());
	}

}