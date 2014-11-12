package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.android.api.Drone;
import org.droidplanner.android.maps.MarkerInfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ox3dr.services.android.lib.drone.property.Home;

public class GraphicHome extends MarkerInfo.SimpleMarkerInfo {

	private Drone drone;

	public GraphicHome(Drone drone) {
		this.drone = drone;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	public boolean isValid() {
        Home droneHome = drone.getHome();
		return droneHome != null && droneHome.isValid();
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return BitmapFactory.decodeResource(res, R.drawable.ic_wp_home);
	}

	@Override
	public com.ox3dr.services.android.lib.coordinate.LatLong getPosition() {
        Home droneHome = drone.getHome();
        if(droneHome == null) return null;

		return droneHome.getCoordinate();
	}

	@Override
	public String getSnippet() {
        Home droneHome = drone.getHome();
		return "Home " + (droneHome == null ? "N/A" : droneHome.getCoordinate().getAltitude());
	}

	@Override
	public String getTitle() {
		return "Home";
	}

	@Override
	public boolean isVisible() {
		return isValid();
	}
}
