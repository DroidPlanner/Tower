package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.helpers.geoTools.PolylineTools;
import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;

public class Grid {
	public List<LatLng> gridPoints;
	private List<LatLng> cameraLocations;
	private Double altitude;

	public Grid(List<LatLng> list, List<LatLng> cameraLocations) {
		this.gridPoints = list;
		this.cameraLocations = cameraLocations;
	}

	public ArrayList<waypoint> getWaypoints() {
		ArrayList<waypoint> list = new ArrayList<waypoint>();
		for (LatLng point : gridPoints) {
			list.add(new waypoint(point, altitude));
		}
		return list;
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

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}
	
}