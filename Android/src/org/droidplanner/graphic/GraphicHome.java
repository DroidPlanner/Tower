package org.droidplanner.graphic;

import org.droidplanner.R.drawable;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.variables.Home;
import org.droidplanner.graphic.markers.MarkerManager.MarkerSource;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GraphicHome implements MarkerSource {

	private Home home;

	public GraphicHome(Drone drone) {
		home = drone.home;
	}

	public boolean isValid() {
		return home.isValid();
	}

	@Override
	public MarkerOptions build(Context context) {
		return new MarkerOptions()
				.position(DroneHelper.CoordToLatLang(home.getCoord()))
				.visible(home.isValid())
				.title("Home")
				.snippet(home.getAltitude().toString())
				.anchor((float) 0.5, (float) 0.5)
				.icon(BitmapDescriptorFactory
						.fromResource(drawable.ic_wp_home)).title("Home");
	}

	@Override
	public void update(Marker marker, Context context) {
		marker.setVisible(home.isValid());
		marker.setPosition(DroneHelper.CoordToLatLang(home.getCoord()));
		marker.setSnippet("Home "+ home.getAltitude());
		
	}

}
