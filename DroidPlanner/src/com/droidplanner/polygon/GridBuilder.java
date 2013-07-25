package com.droidplanner.polygon;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.droidplanner.drone.variables.waypoint;
import com.google.android.gms.maps.model.LatLng;

public class GridBuilder {

	private Polygon poly;
	private Double angle;
	private Double lineDist;
	private LatLng lastLocation;
	private Double altitude;
	private int frame;

	public GridBuilder(Polygon poly, Double angle, Double lineDist,
			LatLng lastLocation, Double altitude, int frame) {
		this.poly = poly;
		this.angle = angle;
		this.lineDist = lineDist;
		this.lastLocation = lastLocation;
		this.altitude = altitude;
		this.frame = frame;
	}

	public List<waypoint> hatchfill() {
		List<LineLatLng> gridLines = generateGrid(poly.getWaypoints(), angle,
				lineDist);
		List<LineLatLng> hatchLines = trimGridLines(poly.getWaypoints(),
				gridLines);
		List<waypoint> gridPoints = waypointsFromHatch(lastLocation, altitude,
				hatchLines);

		return gridPoints;
	}

	/**
	 * Generates a list of waypoints from a list of hatch lines, choosing the
	 * best way to organize the mission. Uses the extreme points of the lines as
	 * waypoints.
	 * 
	 * @param lastLocation
	 *            The last location of the mission, used to chose where to start
	 *            filling the polygon with the hatch
	 * @param altitude
	 *            Altitude of the waypoints
	 * @param hatchLines
	 *            List of lines to be ordered and added
	 */
	private List<waypoint> waypointsFromHatch(LatLng lastLocation,
			Double altitude, List<LineLatLng> hatchLines) {
		List<waypoint> gridPoints = new ArrayList<waypoint>();
		LineLatLng closest = GeoTools.findClosestLine(lastLocation, hatchLines);
		LatLng lastpnt;

		if (GeoTools.getDistance(closest.p1, lastLocation) < GeoTools
				.getDistance(closest.p2, lastLocation)) {
			lastpnt = closest.p1;
		} else {
			lastpnt = closest.p2;
		}

		while (hatchLines.size() > 0) {
			if (GeoTools.getDistance(closest.p1, lastpnt) < GeoTools
					.getDistance(closest.p2, lastpnt)) {
				gridPoints.add(new waypoint(closest.p1, altitude,frame));
				gridPoints.add(new waypoint(closest.p2, altitude,frame));

				lastpnt = closest.p2;

				hatchLines.remove(closest);
				if (hatchLines.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p2, hatchLines);
			} else {
				gridPoints.add(new waypoint(closest.p2, altitude,frame));
				gridPoints.add(new waypoint(closest.p1, altitude,frame));

				lastpnt = closest.p1;

				hatchLines.remove(closest);
				if (hatchLines.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p1, hatchLines);
			}
		}
		return gridPoints;
	}

	/**
	 * Trims a grid of lines for points outside a polygon
	 * 
	 * @param waypoints2
	 *            Polygon vertices
	 * @param grid
	 *            Array with Grid lines
	 * @return array with the trimmed grid lines
	 */
	private List<LineLatLng> trimGridLines(List<LatLng> waypoints2,
			List<LineLatLng> grid) {
		List<LineLatLng> hatchLines = new ArrayList<LineLatLng>();
		// find intersections
		for (LineLatLng gridLine : grid) {
			double closestDistance = Double.MAX_VALUE;
			double farestDistance = Double.MIN_VALUE;

			LatLng closestPoint = null;
			LatLng farestPoint = null;

			int crosses = 0;

			for (int b = 0; b < waypoints2.size(); b++) {
				LatLng newlatlong;
				if (b != waypoints2.size() - 1) {
					newlatlong = GeoTools.FindLineIntersection(
							waypoints2.get(b), waypoints2.get(b + 1),
							gridLine.p1, gridLine.p2);
				} else { // Don't forget the last polygon line
					newlatlong = GeoTools.FindLineIntersection(
							waypoints2.get(b), waypoints2.get(0), gridLine.p1,
							gridLine.p2);
				}

				if (newlatlong != null) {
					crosses++;
					if (closestDistance > GeoTools.getDistance(gridLine.p1,
							newlatlong)) {
						closestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						closestDistance = GeoTools.getDistance(gridLine.p1,
								newlatlong);
					}
					if (farestDistance < GeoTools.getDistance(gridLine.p1,
							newlatlong)) {
						farestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						farestDistance = GeoTools.getDistance(gridLine.p1,
								newlatlong);
					}
				}
			}

			switch (crosses) {
			case 0:
			case 1:
				break;
			default: // TODO handle multiple crossings in a better way
			case 2:
				hatchLines.add(new LineLatLng(closestPoint, farestPoint));
				break;
			}
		}
		return hatchLines;
	}

	/**
	 * Generates a grid over the specified boundary's
	 * 
	 * @param waypoints2
	 *            Array with the polygon points
	 * @param angle
	 *            Angle of the grid in Degrees
	 * @param lineDist
	 *            Distance between lines in meters
	 * @return Returns a array of lines of the generated grid
	 */
	private List<LineLatLng> generateGrid(List<LatLng> waypoints2,
			Double angle, Double lineDist) {
		List<LineLatLng> gridLines = new ArrayList<LineLatLng>();

		PolyBounds bounds = new PolyBounds(waypoints2);
		LatLng point = new LatLng(bounds.getMiddle().latitude,
				bounds.getMiddle().longitude);

		point = GeoTools.newpos(point, angle - 135, bounds.getDiag());

		// get x y step amount in lat lng from m
		Double y1 = Math.cos(Math.toRadians(angle + 90));
		Double x1 = Math.sin(Math.toRadians(angle + 90));
		LatLng diff = new LatLng(GeoTools.metersTolat(lineDist * y1),
				GeoTools.metersTolat(lineDist * x1));
		Log.d("Diff", "Lat:" + GeoTools.metersTolat(lineDist * y1) + " Long:"
				+ GeoTools.metersTolat(lineDist * x1));

		// draw grid
		int lines = 0;
		while (lines * lineDist < bounds.getDiag() * 1.5) {
			LatLng pointx = point;
			pointx = GeoTools.newpos(pointx, angle, bounds.getDiag() * 1.5);

			LineLatLng line = new LineLatLng(point, pointx);
			gridLines.add(line);

			point = GeoTools.addLatLng(point, diff);
			lines++;
		}

		return gridLines;
	}
}
