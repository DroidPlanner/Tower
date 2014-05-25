package org.droidplanner.core.mission.survey.grid;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.LineCoord2D;
import org.droidplanner.core.helpers.geoTools.LineSampler;
import org.droidplanner.core.helpers.geoTools.LineTools;

public class EndpointSorter {
	private static final int MAX_NUMBER_OF_CAMERAS = 2000;

	private List<Coord2D> gridPoints = new ArrayList<Coord2D>();
	private List<LineCoord2D> grid;
	private Double sampleDistance;
	private List<Coord2D> cameraLocations = new ArrayList<Coord2D>();

	public EndpointSorter(List<LineCoord2D> grid, Double sampleDistance) {
		this.grid = grid;
		this.sampleDistance = sampleDistance;
	}

	public void sortGrid(Coord2D lastpnt, boolean innerWPs) throws Exception {
		while (grid.size() > 0) {
			LineCoord2D closestLine = LineTools.findClosestLineToPoint(lastpnt,
					grid);
			Coord2D secondWp = processOneGridLine(closestLine, lastpnt,
					innerWPs);
			lastpnt = secondWp;
		}
	}

	private Coord2D processOneGridLine(LineCoord2D closestLine, Coord2D lastpnt,
			boolean innerWPs) throws Exception {
		Coord2D firstWP = closestLine.getClosestEndpointTo(lastpnt);
		Coord2D secondWp = closestLine.getFarthestEndpointTo(lastpnt);

		grid.remove(closestLine);

		addWaypointsBetween(firstWP, secondWp, innerWPs);
		if (cameraLocations.size() > MAX_NUMBER_OF_CAMERAS) {
			throw new Exception("Too many camera positions");
		}
		return secondWp;
	}

	private void addWaypointsBetween(Coord2D firstWP, Coord2D secondWp,
			boolean innerWPs) {
		List<Coord2D> cameraLocationsOnThisStrip = new LineSampler(firstWP,
				secondWp).sample(sampleDistance);
		cameraLocations.addAll(cameraLocationsOnThisStrip);
		if (innerWPs) {
			for (Coord2D point : cameraLocationsOnThisStrip) {
				gridPoints.add(point);
			}
		} else {
			gridPoints.add(firstWP);
			gridPoints.add(secondWp);
		}
	}

	public List<Coord2D> getSortedGrid() {
		return gridPoints;
	}

	public List<Coord2D> getCameraLocations() {
		return cameraLocations;
	}

}
