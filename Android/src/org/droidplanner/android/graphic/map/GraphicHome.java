package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.variables.Home;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;

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
						.fromResource(R.drawable.ic_wp_home)).title("Home");
	}

	@Override
	public void update(Marker marker, Context context) {
		marker.setVisible(home.isValid());
		marker.setPosition(DroneHelper.CoordToLatLang(home.getCoord()));
		marker.setSnippet("Home " + home.getAltitude());

	}

}
