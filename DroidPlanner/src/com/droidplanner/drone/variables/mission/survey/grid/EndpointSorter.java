package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.droidplanner.helpers.geoTools.LineSampler;
import com.droidplanner.helpers.geoTools.LineTools;
import com.google.android.gms.maps.model.LatLng;

public class EndpointSorter {
	private static final int MAX_NUMBER_OF_CAMERAS = 2000;
	
	private List<LatLng> gridPoints = new ArrayList<LatLng>();
	private List<LineLatLng> grid;
	private Double sampleDistance;
	private List<LatLng> cameraLocations = new ArrayList<LatLng>();

	public EndpointSorter(List<LineLatLng> grid, Double sampleDistance) {
		this.grid = grid;
		this.sampleDistance = sampleDistance;
	}

	public void sortGrid(LatLng lastpnt, boolean innerWPs) throws Exception {
		while (grid.size() > 0) {
			LineLatLng closestLine = LineTools.findClosestLineToPoint(lastpnt,
					grid);
			LatLng secondWp = processOneGridLine(closestLine, lastpnt, innerWPs);
			lastpnt = secondWp;
		}
	}

	private LatLng processOneGridLine(LineLatLng closestLine, LatLng lastpnt,
			boolean innerWPs) throws Exception {
		LatLng firstWP = closestLine.getClosestEndpointTo(lastpnt);
		LatLng secondWp = closestLine.getFarthestEndpointTo(lastpnt);

		grid.remove(closestLine);

		addWaypointsBetween(firstWP, secondWp, innerWPs);
		if (cameraLocations.size()>MAX_NUMBER_OF_CAMERAS) {
			throw new Exception("Too many camera positions");
		}
		return secondWp;
	}

	private void addWaypointsBetween(LatLng firstWP, LatLng secondWp,
			boolean innerWPs) {
		List<LatLng> cameraLocationsOnThisStrip = new LineSampler(firstWP, secondWp)
				.sample(sampleDistance);
		cameraLocations.addAll(cameraLocationsOnThisStrip);
		if (innerWPs) {
			for (LatLng point : cameraLocationsOnThisStrip) {
				gridPoints.add(point);
			}
		} else {
			gridPoints.add(firstWP);
			gridPoints.add(secondWp);
		}
	}

	public List<LatLng> getSortedGrid() {
		return gridPoints;
	}
	
	public List<LatLng> getCameraLocations() {
		return cameraLocations;
	}

}
