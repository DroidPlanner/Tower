package com.droidplanner.MAVLink;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.waypoint;
import com.google.android.gms.maps.model.LatLng;

public class DroneMission {
	public waypoint home;
	public Double defaultAlt;
	public List<waypoint> waypoints;
	public int wpno;
	public double disttowp;

	public DroneMission(int wpno,double disttowp) {
		this.wpno = wpno;
		this.disttowp = disttowp;
	}
	
	public double getDisttowp() {
		return disttowp;
	}
	
	public int getWpno() {
		return wpno;
	}
	
	public Double getDefaultAlt() {
		return defaultAlt;
	}

	public waypoint getHome() {
		return home;
	}

	public waypoint getLastWaypoint() {
		if (waypoints.size() > 0)
			return waypoints.get(waypoints.size() - 1);
		else
			return home;
	}

	public List<LatLng> getAllCoordinates() {
		List<LatLng> result = new ArrayList<LatLng>();
		for (waypoint point : waypoints) {
			result.add(point.coord);
		}
		result.add(home.coord);
		return result;
	}
}