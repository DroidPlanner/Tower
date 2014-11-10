package org.droidplanner.android.graphic.map;

import org.droidplanner.R;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.maps.MarkerInfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ox3dr.services.android.lib.drone.property.Attitude;
import com.ox3dr.services.android.lib.drone.property.Gps;

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
        return isValid() ? droneApi.getGps().getPosition() :  null;
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
        Attitude attitude = droneApi.getAttitude();
		return attitude == null ? 0 : (float) attitude.getYaw();
	}

	public boolean isValid() {
        Gps droneGps = droneApi.getGps();
		return droneGps != null && droneGps.isValid();
	}
}
