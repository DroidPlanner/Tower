package com.droidplanner.polygon;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.MAVLink.waypoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class Polygon {

	private List<LatLng> waypoints;

	public Polygon() {
		setWaypoints(new ArrayList<LatLng>());
	}

	private void setWaypoints(ArrayList<LatLng> arrayList) {
		waypoints = arrayList;
	}

	public void addWaypoint(Double Lat, Double Lng) {
		getWaypoints().add(new LatLng(Lat, Lng));
	}

	public void addWaypoint(LatLng coord) {
		getWaypoints().add(GeoTools.findClosestPair(coord, getWaypoints()), coord);
	}

	public void clearPolygon() {
		getWaypoints().clear();
	}

	class LineLatLng {
		public LatLng p1;
		public LatLng p2;

		public LineLatLng(LatLng p1, LatLng p2) {
			this.p1 = p1;
			this.p2 = p2;
		}
	}

	public List<waypoint> hatchfill(Double angle, Double lineDist,
			LatLng lastLocation, Double altitude) {
		List<LineLatLng> gridLines = generateGrid(getWaypoints(), angle, lineDist);
		List<LineLatLng> hatchLines = trimGridLines(getWaypoints(), gridLines);

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

		if (GeoTools.getDistance(closest.p1, lastLocation) < GeoTools.getDistance(closest.p2,
				lastLocation)) {
			lastpnt = closest.p1;
		} else {
			lastpnt = closest.p2;
		}

		while (hatchLines.size() > 0) {
			if (GeoTools.getDistance(closest.p1, lastpnt) < GeoTools.getDistance(closest.p2,
					lastpnt)) {
				gridPoints.add(new waypoint(closest.p1, altitude));
				gridPoints.add(new waypoint(closest.p2, altitude));

				lastpnt = closest.p2;

				hatchLines.remove(closest);
				if (hatchLines.size() == 0)
					break;
				closest = GeoTools.findClosestLine(closest.p2, hatchLines);
			} else {
				gridPoints.add(new waypoint(closest.p2, altitude));
				gridPoints.add(new waypoint(closest.p1, altitude));

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
					newlatlong = GeoTools.FindLineIntersection(waypoints2.get(b),
							waypoints2.get(b + 1), gridLine.p1, gridLine.p2);
				} else { // Don't forget the last polygon line
					newlatlong = GeoTools.FindLineIntersection(waypoints2.get(b),
							waypoints2.get(0), gridLine.p1, gridLine.p2);
				}

				if (newlatlong != null) {
					crosses++;
					if (closestDistance > GeoTools.getDistance(gridLine.p1, newlatlong)) {
						closestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						closestDistance = GeoTools.getDistance(gridLine.p1, newlatlong);
					}
					if (farestDistance < GeoTools.getDistance(gridLine.p1, newlatlong)) {
						farestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						farestDistance = GeoTools.getDistance(gridLine.p1, newlatlong);
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
	private List<Polygon.LineLatLng> generateGrid(List<LatLng> waypoints2,
			Double angle, Double lineDist) {
		List<Polygon.LineLatLng> gridLines = new ArrayList<Polygon.LineLatLng>();

		Bounds bounds = new Bounds(waypoints2);
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

			Polygon.LineLatLng line = new Polygon.LineLatLng(point, pointx);
			gridLines.add(line);

			point = GeoTools.addLatLng(point, diff);
			lines++;
		}

		return gridLines;
	}

	/**
	 * 
	 * Object for holding boundary for a polygon
	 * 
	 */
	private class Bounds {
		public LatLng sw;
		public LatLng ne;

		public Bounds(List<LatLng> points) {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (LatLng point : points) {
				builder.include(point);
			}
			LatLngBounds bounds = builder.build();
			sw = bounds.southwest;
			ne = bounds.northeast;
		}

		public double getDiag() {
			return GeoTools.latToMeters(GeoTools.getDistance(ne, sw));
		}

		public LatLng getMiddle() {
			return (new LatLng((ne.latitude + sw.latitude) / 2,
					(ne.longitude + sw.longitude) / 2));

		}
	}

	/**
	 * Experimental Function, needs testing! Calculate the area of the polygon
	 * 
	 * @return area in m�
	 */
	// TODO test and fix this function
	public Double getArea() {
		double sum = 0.0;
		for (int i = 0; i < getWaypoints().size() - 1; i++) {
			sum = sum
					+ (GeoTools.latToMeters(getWaypoints().get(i).longitude) * GeoTools.latToMeters(getWaypoints()
							.get(i + 1).latitude))
					- (GeoTools.latToMeters(getWaypoints().get(i).latitude) * GeoTools.latToMeters(getWaypoints()
							.get(i + 1).longitude));
		}
		return Math.abs(0.5 * sum);
	}

	public boolean isValid() {
		if(getWaypoints().size()>2)	// A valid polygon must have at least 3 points
			return true;
		else
			return false;
	}

	public List<LatLng> getWaypoints() {
		return waypoints;
	}

	public void movePoint(LatLng coord, int number) {
		waypoints.set(number, coord);
		
	}
}
