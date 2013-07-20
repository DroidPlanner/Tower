package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.maps.model.LatLng;

public class waypoint {	
	public msg_mission_item missionItem = new msg_mission_item();

	public waypoint(LatLng c, Double h) {
		this(c.latitude, c.longitude, h);
	}

	public waypoint(Double Lat, Double Lng, Double h) {
		setCoord(new LatLng(Lat, Lng));
		setHeight(h);
	}

	public Double getHeight() {
		return (double) missionItem.z;
	}

	public LatLng getCoord() {
		return new LatLng(missionItem.x, missionItem.y);
	}

	public void setHeight(Double height) {
		missionItem.z = height.floatValue();
	}

	public void setCoord(LatLng coord) {
		missionItem.x = (float) coord.latitude;
		missionItem.y = (float) coord.longitude;
	}
}