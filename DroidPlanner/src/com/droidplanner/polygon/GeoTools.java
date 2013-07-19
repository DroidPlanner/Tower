package com.droidplanner.polygon;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class GeoTools {
	public List<LatLng> waypoints;

	public GeoTools() {
	}

	/**
	 * Provides the distance from a point P to the line segment that passes
	 * through A-B. If the point is not on the side of the line, returns the
	 * distance to the closest point
	 * @param L1 First point of the line
	 * @param L2 Second point of the line
	 * @param P  Point to measure the distance
	 */
	public static double pointToLineDistance(LatLng L1, LatLng L2, LatLng P) {
		double A = P.longitude - L1.longitude;
		double B = P.latitude - L1.latitude;
		double C = L2.longitude - L1.longitude;
		double D = L2.latitude - L1.latitude;
	
		double dot = A * C + B * D;
		double len_sq = C * C + D * D;
		double param = dot / len_sq;
	
		double xx, yy;
	
		if (param < 0) // point behind the segment
		{
			xx = L1.longitude;
			yy = L1.latitude;
		} else if (param > 1) // point after the segment
		{
			xx = L2.longitude;
			yy = L2.latitude;
		} else { // point on the side of the segment
			xx = L1.longitude + param * C;
			yy = L1.latitude + param * D;
		}
	
		return Math.hypot(xx - P.longitude, yy - P.latitude);
	}

	/**
	 * Adds an offset to a point (in degrees)
	 * 
	 * @param point
	 *            the point to be modified
	 * @param offset
	 *            offset to be added
	 * @return point with offset
	 */
	static LatLng addLatLng(LatLng point, LatLng offset) {
		return (new LatLng(point.latitude + offset.latitude, point.longitude
				+ offset.longitude));
	}

	/**
	 * Returns the distance between two points
	 * 
	 * @return distance between the points in degrees
	 */
	static Double getDistance(LatLng p1, LatLng p2) {
		return (Math.hypot((p1.latitude - p2.latitude),
				(p1.longitude - p2.longitude)));
	}

	static Double metersTolat(double meters) {
		double radius_of_earth = 6378100.0;// # in meters
		return Math.toDegrees(meters / radius_of_earth);
	}

	static Double latToMeters(double lat) {
		double radius_of_earth = 6378100.0;// # in meters
		return Math.toRadians(lat) * radius_of_earth;
	}

	/**
	 * Extrapolate latitude/longitude given a heading and distance thanks to
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param origin
	 *            Point of origin
	 * @param bearing
	 *            bearing to navigate
	 * @param distance
	 *            distance to be added
	 * @return New point with the added distance
	 */
	static LatLng newpos(LatLng origin, double bearing, double distance) {
		double radius_of_earth = 6378100.0;// # in meters
	
		double lat = origin.latitude;
		double lon = origin.longitude;
		double lat1 = Math.toRadians(lat);
		double lon1 = Math.toRadians(lon);
		double brng = Math.toRadians(bearing);
		double dr = distance / radius_of_earth;
	
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1)
				* Math.sin(dr) * Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1),
						Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));
	
		return (new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2)));
	}

	/**
	 * Finds the pair of adjacent points that minimize the distance to a
	 * reference point
	 * 
	 * @param point
	 *            point that will be used as reference
	 * @param waypoints2
	 *            List of points to be searched
	 * @return Position of the second point in the pair that minimizes the
	 *         distance
	 */
	static int findClosestPair(LatLng point, List<LatLng> waypoints2) {
		int answer = 0;
		double currentbest = Double.MAX_VALUE;
		double dist;
		LatLng p1, p2;
	
		for (int i = 0; i < waypoints2.size(); i++) {
			if (i == waypoints2.size() - 1) {
				p1 = waypoints2.get(i);
				p2 = waypoints2.get(0);
			} else {
				p1 = waypoints2.get(i);
				p2 = waypoints2.get(i + 1);
			}
	
			dist = pointToLineDistance(p1, p2, point);
			if (dist < currentbest) {
				answer = i + 1;
				currentbest = dist;
			}
		}
		return answer;
	}

	/**
	 * Finds the closest point in a list to another point
	 * 
	 * @param point
	 *            point that will be used as reference
	 * @param list
	 *            List of points to be searched
	 * @return The closest point
	 */
	@SuppressWarnings("unused")
	private static LatLng findClosestPoint(LatLng point, List<LatLng> list) {
		LatLng answer = null;
		double currentbest = Double.MAX_VALUE;
	
		for (LatLng pnt : list) {
			double dist1 = getDistance(point, pnt);
	
			if (dist1 < currentbest) {
				answer = pnt;
				currentbest = dist1;
			}
		}
		return answer;
	}

	/**
	 * Finds the line that has the start or tip closest to a point.
	 * 
	 * @param point
	 *            Point to the distance will be minimized
	 * @param list
	 *            A list of lines to search
	 * @return The closest Line
	 */
	static LineLatLng findClosestLine(LatLng point, List<LineLatLng> list) {
		LineLatLng answer = list.get(0);
		double shortest = Double.MAX_VALUE;
	
		for (LineLatLng line : list) {
			double ans1 = getDistance(point, line.p1);
			double ans2 = getDistance(point, line.p2);
			LatLng shorterpnt = ans1 < ans2 ? line.p1 : line.p2;
	
			if (shortest > getDistance(point, shorterpnt)) {
				answer = line;
				shortest = getDistance(point, shorterpnt);
			}
		}
		return answer;
	}

	/**
	 * Finds the intersection of two lines http://stackoverflow.com/questions/
	 * 1119451/how-to-tell-if-a-line-intersects -a-polygon-in-c
	 * 
	 * @param start1
	 *            starting point of the first line
	 * @param end1
	 *            ending point of the first line
	 * @param start2
	 *            starting point of the second line
	 * @param end2
	 *            ending point of the second line
	 * @return point of intersection, or null if there is no intersection
	 */
	static LatLng FindLineIntersection(LatLng start1, LatLng end1,
			LatLng start2, LatLng end2) {
		double denom = ((end1.longitude - start1.longitude) * (end2.latitude - start2.latitude))
				- ((end1.latitude - start1.latitude) * (end2.longitude - start2.longitude));
		// AB & CD are parallel
		if (denom == 0)
			return null;
		double numer = ((start1.latitude - start2.latitude) * (end2.longitude - start2.longitude))
				- ((start1.longitude - start2.longitude) * (end2.latitude - start2.latitude));
		double r = numer / denom;
		double numer2 = ((start1.latitude - start2.latitude) * (end1.longitude - start1.longitude))
				- ((start1.longitude - start2.longitude) * (end1.latitude - start1.latitude));
		double s = numer2 / denom;
		if ((r < 0 || r > 1) || (s < 0 || s > 1))
			return null;
		// Find intersection point
		double longitude = start1.longitude
				+ (r * (end1.longitude - start1.longitude));
		double latitude = start1.latitude
				+ (r * (end1.latitude - start1.latitude));
		return (new LatLng(latitude, longitude));
	}

	/**
	 * Experimental Function, needs testing! Calculate the area of the polygon
	 * 
	 * @return area in m�
	 */
	// TODO test and fix this function
	public static  Double getArea(Polygon poly) {
		double sum = 0.0;
		for (int i = 0; i < poly.getWaypoints().size() - 1; i++) {
			sum = sum
					+ (latToMeters(poly.getWaypoints().get(i).longitude) * latToMeters(poly.getWaypoints()
							.get(i + 1).latitude))
					- (latToMeters(poly.getWaypoints().get(i).latitude) * latToMeters(poly.getWaypoints()
							.get(i + 1).longitude));
		}
		return Math.abs(0.5 * sum);
	}
}