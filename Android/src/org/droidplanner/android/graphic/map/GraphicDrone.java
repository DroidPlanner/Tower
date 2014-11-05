package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.maps.MarkerInfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GraphicDrone extends MarkerInfo.SimpleMarkerInfo {

	private DroneApi droneApi;

	public GraphicDrone(DroneApi drone) {
		this.droneApi = drone;
	}

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public com.ox3dr.services.android.lib.coordinate.LatLong getPosition() {
		return droneApi.getGps().getPosition();
	}

	@Override
	public Bitmap getIcon(Resources res) {
		if (droneApi.isConnected()) {
			return BitmapFactory.decodeResource(res, R.drawable.quad);
		}
		return BitmapFactory.decodeResource(res, R.drawable.quad_disconnect);

	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}

	@Override
	public float getRotation() {
		return (float) droneApi.getAttitude().getYaw();
	}

	public boolean isValid() {
		return droneApi.getGps().isValid();
	}
}
