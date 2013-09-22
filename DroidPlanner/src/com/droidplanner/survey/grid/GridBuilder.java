package com.droidplanner.survey.grid;

import java.util.List;

import com.droidplanner.helpers.geoTools.LineLatLng;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.survey.SurveyData;
import com.google.android.gms.maps.model.LatLng;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private LatLng origin;
	private boolean innerWPs;
	private Double wpDistance;

	private Grid grid;

	public GridBuilder(Polygon polygon, SurveyData surveyData,
			LatLng originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = surveyData.getAngle();
		this.lineDist = surveyData.getLateralPictureDistance().valueInMeters();
		this.innerWPs = surveyData.shouldGenerateInnerWPs();
		this.wpDistance = surveyData.getLongitudinalPictureDistance()
				.valueInMeters();
	}

	public GridBuilder(Polygon polygon, double angle, double distance,
			LatLng originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = angle;
		this.lineDist = distance;
		this.innerWPs = false;
	}

	public Grid generate() throws Exception {
		List<LatLng> polygonPoints = poly.getLatLngList();

		List<LineLatLng> circumscribedGrid = new CircumscribedGrid(
				polygonPoints, angle, lineDist).getGrid();
		List<LineLatLng> trimedGrid = new Trimmer(circumscribedGrid,
				poly.getLines()).getTrimmedGrid();
		EndpointSorter gridSorter = new EndpointSorter(trimedGrid, wpDistance);
		gridSorter.sortGrid(origin, innerWPs);
		grid = new Grid(gridSorter.getSortedGrid(),gridSorter.getCameraLocations());
		return grid;
	}

}
