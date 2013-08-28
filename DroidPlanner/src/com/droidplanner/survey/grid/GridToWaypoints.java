package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class GridToWaypoints {

	/**
	 * Generates a list of waypoints from a list of hatch lines, choosing the
	 * best way to organize the mission. Uses the extreme points of the lines as
	 * waypoints.
	 * 
	 * @param lastLocation
	 *            The last location of the mission, used to chose where to start
	 *            filling the polygon with the hatch
	 * @param altitude
	 *            Altitude of the waypoints
	 * @param hatchLines
	 *            List of lines to be ordered and added
	 */
	static List<waypoint> waypointsFromGrid(LatLng lastLocation,
			Double altitude, List<LineLatLng> hatchLines) {
		List<waypoint> gridPoints = new ArrayList<waypoint>();
		LineLatLng closest = GeoTools.findClosestLine(lastLocation, hatchLines);
		LatLng lastpnt;
	
		if (GeoTools.getAproximatedDistance(closest.p1, lastLocation) < GeoTools
				.getAproximatedDistance(closest.p2, lastLocation)) {
			lastpnt = closest.p1;
		} else {
			lastpnt = closest.p2;
		}
	
		while (hatchLines.size() > 0) {
			if (GeoTools.getAproximatedDistance(closest.p1, lastpnt) < GeoTools
					.getAproximatedDistance(closest.p2, lastpnt)) {
				gridPoints.add(new waypoint(closest.p1, altitude));
				//TODO add the code to generate the inner waypoints if necessary.
				gridPoints.add(new waypoint(closest.p2, altitude));
				
				lastpnt = closest.p2;
	
				hatchLines.remove(closest);
				if (hatchLines.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p2, hatchLines);
			} else {
				gridPoints.add(new waypoint(closest.p2, altitude));
				//TODO add the code to generate the inner waypoints if necessary.
				gridPoints.add(new waypoint(closest.p1, altitude));
	
				lastpnt = closest.p1;
	
				hatchLines.remove(closest);
				if (hatchLines.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p1, hatchLines);
			}
		}
		return gridPoints;
	}

}
