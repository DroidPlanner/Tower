package com.droidplanner.drone.variables;

import com.droidplanner.fragments.markers.HomeMarker;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class Home extends waypoint {
	boolean isValid = false;
	
	public Home() {
		super(0.0, 0.0, 0.0);
		setCurrent((byte) 1); // TODO Use correct parameter for home
	}
	
	public Home(Double Lat, Double Lng, Double h) {
		super(Lat, Lng, h);
		isValid = true;
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

	public void updateData(waypoint wp) {
		this.missionItem = wp.missionItem;
		isValid = true;
	}

	public boolean isValid() {
		return isValid;
	}

}
