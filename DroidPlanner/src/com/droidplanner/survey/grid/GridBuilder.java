package com.droidplanner.survey.grid;

import java.util.List;

import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.polygon.Polygon;
import com.google.android.gms.maps.model.LatLng;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private LatLng lastLocation;
	private Double altitude;
	//private boolean innerWPs;

	private List<waypoint> gridPoints;

	public GridBuilder(Polygon poly, Double angle, Double lineDist,
			LatLng lastLocation, Double altitude) {
		this.poly = poly;
		this.angle = angle;
		this.lineDist = lineDist;
		this.lastLocation = lastLocation;
		this.altitude = altitude;
	}

	public void setGenerateInnerWaypoints(boolean innerWPs) {
		//this.innerWPs = innerWPs;
	}

	public List<waypoint> generate() {
		List<LatLng> polygonPoints = poly.getLatLngList();

		List<LineLatLng> circumscribedGrid = new Generator(polygonPoints, angle,
				lineDist).getGrid();
		List<LineLatLng> trimedGrid = new Trimmer(circumscribedGrid, polygonPoints)
				.getTrimmedGrid();
		EndpointSorter gridSorter = new EndpointSorter(trimedGrid, lastLocation, altitude);
		gridSorter.sortGrid();		 
		gridPoints = gridSorter.getWaypoints();
		return gridPoints;
	}
}
