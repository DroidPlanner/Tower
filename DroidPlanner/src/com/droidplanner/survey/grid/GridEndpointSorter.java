package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class GridEndpointSorter {
	private List<waypoint> gridPoints = new ArrayList<waypoint>();
	private List<LineLatLng> grid;
	private LatLng lastpnt;
	private LineLatLng firstLine;
	private Double altitude;

	public GridEndpointSorter(List<LineLatLng> grid, LatLng start,
			Double altitude) {
		this.grid = grid;
		this.altitude = altitude;
		firstLine = GeoTools.findClosestLineToPoint(start, grid);
		lastpnt = firstLine.getClosestEndpointTo(start);
	}

	public void sortGrid() {
		LineLatLng closestLine = new LineLatLng(firstLine);
		while (grid.size() > 0) {
			if (GeoTools.getAproximatedDistance(closestLine.p1, lastpnt) < GeoTools
					.getAproximatedDistance(closestLine.p2, lastpnt)) {
				gridPoints.add(new waypoint(closestLine.p1, altitude));
				// TODO add the code to generate the inner waypoints if
				// necessary.
				gridPoints.add(new waypoint(closestLine.p2, altitude));

				lastpnt = closestLine.p2;

				grid.remove(closestLine);
				if (grid.size() == 0)
					break;
				closestLine = GeoTools.findClosestLineToPoint(closestLine.p2, grid);
			} else {
				gridPoints.add(new waypoint(closestLine.p2, altitude));
				// TODO add the code to generate the inner waypoints if
				// necessary.
				gridPoints.add(new waypoint(closestLine.p1, altitude));

				lastpnt = closestLine.p1;

				grid.remove(closestLine);
				if (grid.size() == 0)
					break;
				closestLine = GeoTools.findClosestLineToPoint(closestLine.p1, grid);
			}
		}
	}

	public List<waypoint> getWaypoints() {
		return gridPoints;
	}

}
