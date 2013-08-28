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
	private LineLatLng closest;
	private Double altitude;

	public GridEndpointSorter(List<LineLatLng> grid, LatLng start,
			Double altitude) {
		this.grid = grid;
		this.altitude = altitude;
		closest = GeoTools.findClosestLineToPoint(start, grid);
		lastpnt = closest.getClosestEndpointTo(start);
	}

	public void sortGrid() {
		while (grid.size() > 0) {
			if (GeoTools.getAproximatedDistance(closest.p1, lastpnt) < GeoTools
					.getAproximatedDistance(closest.p2, lastpnt)) {
				gridPoints.add(new waypoint(closest.p1, altitude));
				// TODO add the code to generate the inner waypoints if
				// necessary.
				gridPoints.add(new waypoint(closest.p2, altitude));

				lastpnt = closest.p2;

				grid.remove(closest);
				if (grid.size() == 0)
					break;
				closest = GeoTools.findClosestLineToPoint(closest.p2, grid);
			} else {
				gridPoints.add(new waypoint(closest.p2, altitude));
				// TODO add the code to generate the inner waypoints if
				// necessary.
				gridPoints.add(new waypoint(closest.p1, altitude));

				lastpnt = closest.p1;

				grid.remove(closest);
				if (grid.size() == 0)
					break;
				closest = GeoTools.findClosestLineToPoint(closest.p1, grid);
			}
		}
	}

	public List<waypoint> getWaypoints() {
		return gridPoints;
	}

}
