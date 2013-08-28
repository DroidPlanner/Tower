package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.model.LatLng;

public class Grid {
	public List<LatLng> gridPoints;

	public Grid(List<LatLng> list) {
		this.gridPoints = list;
	}

	public ArrayList<waypoint> getWaypoints(Double altitude) {
		ArrayList<waypoint> list = new ArrayList<waypoint>();
		for (LatLng point : gridPoints) {
			list.add(new waypoint(point, altitude));
		}
		return list;
	}
}