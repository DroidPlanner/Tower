package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.maps.MarkerInfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ox3dr.services.android.lib.drone.property.Home;

public class GraphicHome extends MarkerInfo.SimpleMarkerInfo {

	private Home home;

	public GraphicHome(DroneApi drone) {
		home = drone.getHome();
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	public boolean isValid() {
		return home.isValid();
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
		return home.getCoordinate();
	}

	@Override
	public String getSnippet() {
		return "Home " + home.getCoordinate().getAltitude();
	}

	@Override
	public String getTitle() {
		return "Home";
	}

	@Override
	public boolean isVisible() {
		return home.isValid();
	}
}
