package com.droidplanner.drone.variables.mission.survey.grid;

import java.util.List;

import com.droidplanner.helpers.geoTools.PolylineTools;
import com.droidplanner.helpers.units.Altitude;
import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;

public class Grid {
	public List<LatLng> gridPoints;
	private List<LatLng> cameraLocations;
	private Altitude altitude;

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

	public void setAltitude(Altitude altitude) {
		this.altitude = altitude;
	}

	public int getCameraCount() {
		return getCameraLocations().size();
	}
	
}