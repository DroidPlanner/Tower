package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.droidplanner.helpers.geoTools.LineSampler;
import com.droidplanner.helpers.geoTools.LineTools;
import com.google.android.gms.maps.model.LatLng;

public class EndpointSorter {
	private List<LatLng> gridPoints = new ArrayList<LatLng>();
	private List<LineLatLng> grid;
	private Double sampleDistance;

	public EndpointSorter(List<LineLatLng> grid, Double sampleDistance) {
		this.grid = grid;
		this.sampleDistance = sampleDistance;
	}

	public void sortGrid(LatLng lastpnt, boolean innerWPs) {
		while (grid.size() > 0) {
			LineLatLng closestLine = LineTools.findClosestLineToPoint(lastpnt,
					grid);
			LatLng secondWp = processOneGridLine(closestLine, lastpnt, innerWPs);
			lastpnt = secondWp;
		}
	}

	private LatLng processOneGridLine(LineLatLng closestLine, LatLng lastpnt,
			boolean innerWPs) {
		LatLng firstWP = closestLine.getClosestEndpointTo(lastpnt);
		LatLng secondWp = closestLine.getFarthestEndpointTo(lastpnt);

		grid.remove(closestLine);

		addWaypointsBetween(firstWP, secondWp, innerWPs);
		return secondWp;
	}

	private void addWaypointsBetween(LatLng firstWP, LatLng secondWp,
			boolean innerWPs) {
		if (innerWPs) {
			List<LatLng> list = new LineSampler(firstWP, secondWp)
					.sample(sampleDistance);
			for (LatLng point : list) {
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

}
