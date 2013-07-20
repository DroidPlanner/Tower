package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.maps.model.LatLng;

public class waypoint extends msg_mission_item {
	private static final long serialVersionUID = 1L;

	public waypoint(LatLng c, Double h) {
		this(c.latitude, c.longitude, h);
	}

	public waypoint(Double Lat, Double Lng, Double h) {
		setCoord(new LatLng(Lat, Lng));
		setHeight(h);
	}

	public Double getHeight() {
		return (double) z;
	}

	public LatLng getCoord() {
		return new LatLng(x, y);
	}

	public void setHeight(Double height) {
		z = height.floatValue();
	}

	public void setCoord(LatLng coord) {
		x = (float) coord.latitude;
		y = (float) coord.longitude;
	}
}