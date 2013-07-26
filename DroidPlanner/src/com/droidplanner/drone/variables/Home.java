package com.droidplanner.drone.variables;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.droidplanner.fragments.markers.HomeMarker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Home extends waypoint {
	public Home(LatLng c, Double h, int frame) {
		super(c, h,frame);
		setCurrent((byte) 1); // TODO Use correct parameter for home
	}

	public Home(msg_mission_item msg) {
		super(msg);
		setCurrent((byte) 1); // TODO Use correct parameter for home
	}

	public Home(Double Lat, Double Lng, Double h, int frame) {
		super(Lat, Lng, h, frame);
		setCurrent((byte) 1); // TODO Use correct parameter for home
	}

	public Home(waypoint waypoint) {
		super(waypoint.missionItem);
		setCurrent((byte) 1); // TODO Use correct parameter for home
	}

	@Override
	public MarkerOptions build() {
		return HomeMarker.build(this);
	}

	@Override
	public void update(Marker marker) {
		HomeMarker.update(marker, this);
	}

}
