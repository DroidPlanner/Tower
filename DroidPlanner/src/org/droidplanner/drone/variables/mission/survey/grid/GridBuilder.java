package org.droidplanner.drone.variables.mission.survey.grid;

import java.util.List;

import org.droidplanner.drone.variables.mission.survey.SurveyData;
import org.droidplanner.helpers.geoTools.LineLatLng;
import org.droidplanner.polygon.Polygon;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private LatLng origin;
	private boolean innerWPs;
	private Double wpDistance;
	private Context currContext;

	private Grid grid;

	public GridBuilder(Polygon polygon, SurveyData surveyData,
			LatLng originPoint,Context usedContext) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = surveyData.getAngle();
		this.lineDist = surveyData.getLateralPictureDistance().valueInMeters();
		this.innerWPs = surveyData.shouldGenerateInnerWPs();
		this.wpDistance = surveyData.getLongitudinalPictureDistance()
				.valueInMeters();
		this.currContext = usedContext;
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
				polygonPoints, angle, lineDist,currContext).getGrid();
		List<LineLatLng> trimedGrid = new Trimmer(circumscribedGrid,
				poly.getLines()).getTrimmedGrid();
		EndpointSorter gridSorter = new EndpointSorter(trimedGrid, wpDistance);
		gridSorter.sortGrid(origin, innerWPs);
		grid = new Grid(gridSorter.getSortedGrid(),gridSorter.getCameraLocations());
		return grid;
	}

}
