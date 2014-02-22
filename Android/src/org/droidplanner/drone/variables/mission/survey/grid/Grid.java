package org.droidplanner.drone.variables.mission.survey.grid;

import java.util.List;

import org.droidplanner.helpers.geoTools.PolylineTools;
import org.droidplanner.helpers.units.Length;

import com.google.android.gms.maps.model.LatLng;

public class Grid {
	public List<LatLng> gridPoints;
	private List<LatLng> cameraLocations;

	public Grid(List<LatLng> list, List<LatLng> cameraLocations) {
		this.gridPoints = list;
		this.cameraLocations = cameraLocations;
	}
	
	public Length getLength(){
		return PolylineTools.getPolylineLength(gridPoints);
	}
	
	public int getNumberOfLines(){
		return gridPoints.size()/2;
	}

	public List<LatLng> getCameraLocations() {
		return cameraLocations;
	}

	public int getCameraCount() {
		return getCameraLocations().size();
	}
	
}