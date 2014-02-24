package org.droidplanner.mission.survey.grid;

import java.util.List;

import org.droidplanner.helpers.coordinates.Coord2D;
import org.droidplanner.helpers.geoTools.PolylineTools;
import org.droidplanner.helpers.units.Length;

public class Grid {
	public List<Coord2D> gridPoints;
	private List<Coord2D> cameraLocations;

	public Grid(List<Coord2D> list, List<Coord2D> cameraLocations) {
		this.gridPoints = list;
		this.cameraLocations = cameraLocations;
	}
	
	public Length getLength(){
		return PolylineTools.getPolylineLength(gridPoints);
	}
	
	public int getNumberOfLines(){
		return gridPoints.size()/2;
	}

	public List<Coord2D> getCameraLocations() {
		return cameraLocations;
	}

	public int getCameraCount() {
		return getCameraLocations().size();
	}
	
}