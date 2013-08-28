package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class EndpointSorter {
	private List<waypoint> gridPoints = new ArrayList<waypoint>();
	private List<LineLatLng> grid;
	private Double altitude;

	public EndpointSorter(List<LineLatLng> grid, Double altitude) {
		this.grid = grid;
		this.altitude = altitude;
	}

	public void sortGrid(LatLng lastpnt) {
		while (grid.size() > 0) {
			LineLatLng closestLine = GeoTools.findClosestLineToPoint(lastpnt,
					grid);
			LatLng secondWp = processOneGridLine(closestLine, lastpnt);
			lastpnt = secondWp;
		}
	}

	private LatLng processOneGridLine(LineLatLng closestLine, LatLng lastpnt) {
		LatLng firstWP = closestLine.getClosestEndpointTo(lastpnt);
		LatLng secondWp = closestLine.getFarthestEndpointTo(lastpnt);

		grid.remove(closestLine);

		addWaypointsBetween(firstWP, secondWp);
		return secondWp;
	}

	private void addWaypointsBetween(LatLng firstWP, LatLng secondWp) {
		gridPoints.add(new waypoint(firstWP, altitude));
		// TODO add the code to generate the inner waypoints if necessary.
		gridPoints.add(new waypoint(secondWp, altitude));
	}

	public List<waypoint> getWaypoints() {
		return gridPoints;
	}

}
