package com.droidplanner.drone.variables;

import com.droidplanner.fragments.markers.HomeMarker;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class Home extends waypoint {	
	public Home(Double Lat, Double Lng, Double h) {
		super(Lat, Lng, h);
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
