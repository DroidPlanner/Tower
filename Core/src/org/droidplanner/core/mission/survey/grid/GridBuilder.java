package org.droidplanner.core.mission.survey.grid;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.LineCoord2D;
import org.droidplanner.core.mission.survey.SurveyData;
import org.droidplanner.core.polygon.Polygon;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private Coord2D origin;
	private boolean innerWPs;
	private Double wpDistance;

	private Grid grid;

	public GridBuilder(Polygon polygon, SurveyData surveyData,
			Coord2D originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = surveyData.getAngle();
		this.lineDist = surveyData.getLateralPictureDistance().valueInMeters();
		this.innerWPs = surveyData.shouldGenerateInnerWPs();
		this.wpDistance = surveyData.getLongitudinalPictureDistance()
				.valueInMeters();
	}

	public GridBuilder(Polygon polygon, double angle, double distance,
			Coord2D originPoint) {
		this.poly = polygon;
		this.origin = originPoint;
		this.angle = angle;
		this.lineDist = distance;
		this.innerWPs = false;
	}

	public Grid generate() throws Exception {
		List<Coord2D> polygonPoints = poly.getPoints();

		List<LineCoord2D> circumscribedGrid = new CircumscribedGrid(
				polygonPoints, angle, lineDist).getGrid();
		List<LineCoord2D> trimedGrid = new Trimmer(circumscribedGrid,
				poly.getLines()).getTrimmedGrid();
		EndpointSorter gridSorter = new EndpointSorter(trimedGrid, wpDistance);
		gridSorter.sortGrid(origin, innerWPs);
		grid = new Grid(gridSorter.getSortedGrid(),
				gridSorter.getCameraLocations());
		return grid;
	}

}
