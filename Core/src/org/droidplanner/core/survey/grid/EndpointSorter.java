package org.droidplanner.core.survey.grid;

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

	public void sortGrid(Coord2D lastpnt, boolean sort) throws Exception {
		while (grid.size() > 0) {
			if (sort) {				
				LineCoord2D closestLine = LineTools.findClosestLineToPoint(lastpnt, grid);
				Coord2D secondWp = processOneGridLine(closestLine, lastpnt, sort);
				lastpnt = secondWp;
			}else{
				LineCoord2D closestLine = grid.get(0);
				Coord2D secondWp = processOneGridLine(closestLine, lastpnt, sort);
				lastpnt = secondWp;
			}
		}
	}

	private Coord2D processOneGridLine(LineCoord2D closestLine, Coord2D lastpnt, boolean sort)
			throws Exception {
		Coord2D firstWP, secondWp;
		firstWP = closestLine.getClosestEndpointTo(lastpnt);
		secondWp = closestLine.getFarthestEndpointTo(lastpnt);

		grid.remove(closestLine);

		updateCameraLocations(firstWP, secondWp);
		gridPoints.add(firstWP);
		gridPoints.add(secondWp);
		
		if (cameraLocations.size() > MAX_NUMBER_OF_CAMERAS) {
			throw new Exception("Too many camera positions");
		}
		return secondWp;
	}

	private void updateCameraLocations(Coord2D firstWP, Coord2D secondWp) {
		List<Coord2D> cameraLocationsOnThisStrip = new LineSampler(firstWP, secondWp)
				.sample(sampleDistance);
		cameraLocations.addAll(cameraLocationsOnThisStrip);
	}

	public List<Coord2D> getSortedGrid() {
		return gridPoints;
	}

	public List<Coord2D> getCameraLocations() {
		return cameraLocations;
	}

}
