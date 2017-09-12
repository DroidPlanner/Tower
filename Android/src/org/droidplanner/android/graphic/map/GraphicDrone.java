package org.droidplanner.android.graphic.map;

import android.content.Context;
import android.content.SharedPreferences;
import com.o3dr.services.android.lib.drone.property.Type;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.maps.MarkerInfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;

public class GraphicDrone extends MarkerInfo {

	private Drone drone;
	private SharedPreferences preferences;

	public GraphicDrone(Drone drone) {
		this.drone = drone;
	}

	public GraphicDrone(Drone drone, Context context) {
		this.drone = drone;
		preferences = context.getSharedPreferences
				("towerPrefsKey", android.content.Context.MODE_PRIVATE);
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
	public LatLong getPosition() {
        Gps droneGps = drone.getAttribute(AttributeType.GPS);
        return isValid() ? droneGps.getPosition() :  null;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		boolean showVehicleSpecificIcons = preferences.getBoolean
				(SettingsFragment.VEHICLE_SPECIFIC_ICON_PREF_KEY, false);
		if (drone.isConnected()) {
			if(showVehicleSpecificIcons) {
				Type droneType = drone.getAttribute(AttributeType.TYPE);
				return BitmapFactory.decodeResource(res, updateIcon(droneType));
			} else {
				return BitmapFactory.decodeResource(res, R.drawable.quad);
			}
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
        Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
		return attitude == null ? 0 : (float) attitude.getYaw();
	}

	public boolean isValid() {
        Gps droneGps = drone.getAttribute(AttributeType.GPS);
		return droneGps != null && droneGps.isValid();
	}

	private int updateIcon(Type droneType) {
		int drawable;
		switch (droneType.getDroneType()) {
			case Type.TYPE_COPTER:
				drawable = R.drawable.redcopter;
				break;

			case Type.TYPE_PLANE:
				drawable = R.drawable.redplane;
				break;

			case Type.TYPE_ROVER:
				drawable = R.drawable.redrover;
				break;

			case Type.TYPE_UNKNOWN:
			default:
				drawable = R.drawable.quad;
				break;
		}
		return drawable;
	}
}