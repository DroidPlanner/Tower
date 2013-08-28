package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class GridEndpointSorter {
	List<waypoint> gridPoints = new ArrayList<waypoint>();

	public GridEndpointSorter(List<LineLatLng> grid, LatLng start,
			Double altitude) {
		LineLatLng closest = GeoTools.findClosestLine(start, grid);
		LatLng lastpnt;
	
		if (GeoTools.getAproximatedDistance(closest.p1, start) < GeoTools
				.getAproximatedDistance(closest.p2, start)) {
			lastpnt = closest.p1;
		} else {
			lastpnt = closest.p2;
		}
	
		while (grid.size() > 0) {
			if (GeoTools.getAproximatedDistance(closest.p1, lastpnt) < GeoTools
					.getAproximatedDistance(closest.p2, lastpnt)) {
				gridPoints.add(new waypoint(closest.p1, altitude));
				//TODO add the code to generate the inner waypoints if necessary.
				gridPoints.add(new waypoint(closest.p2, altitude));
				
				lastpnt = closest.p2;
	
				grid.remove(closest);
				if (grid.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p2, grid);
			} else {
				gridPoints.add(new waypoint(closest.p2, altitude));
				//TODO add the code to generate the inner waypoints if necessary.
				gridPoints.add(new waypoint(closest.p1, altitude));
	
				lastpnt = closest.p1;
	
				grid.remove(closest);
				if (grid.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p1, grid);
			}
		}
	}

	public List<waypoint> getWaypoints() {
		return gridPoints;
	}

}
