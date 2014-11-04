package org.droidplanner.core.helpers.geoTools;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.math.MathUtil;
import org.droidplanner.core.helpers.units.Area;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.polygon.Polygon;

public class GeoTools {
	private static final double RADIUS_OF_EARTH = 6378137.0;// In meters.
															// Source: WGS84
	public List<Coord2D> waypoints;

	public GeoTools() {
	}

	/**
	 * Returns the distance between two points
	 * 
	 * @return distance between the points in degrees
	 */
	public static Double getAproximatedDistance(Coord2D p1, Coord2D p2) {
		return (Math.hypot((p1.getX() - p2.getX()), (p1.getY() - p2.getY())));
	}

	public static Double metersTolat(double meters) {
		return Math.toDegrees(meters / RADIUS_OF_EARTH);
	}

	public static Double latToMeters(double lat) {
		return Math.toRadians(lat) * RADIUS_OF_EARTH;
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
	public static Coord2D newCoordFromBearingAndDistance(Coord2D origin, double bearing,
			double distance) {

		double lat = origin.getLat();
		double lon = origin.getLng();
		double lat1 = Math.toRadians(lat);
		double lon1 = Math.toRadians(lon);
		double brng = Math.toRadians(bearing);
		double dr = distance / RADIUS_OF_EARTH;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1) * Math.sin(dr)
				* Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1),
						Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));

		return (new Coord2D(Math.toDegrees(lat2), Math.toDegrees(lon2)));
	}

	/**
	 * Calculates the arc between two points
	 * http://en.wikipedia.org/wiki/Haversine_formula
	 * 
	 * @return the arc in degrees
	 */
	static double getArcInRadians(Coord2D from, Coord2D to) {

		double latitudeArc = Math.toRadians(from.getLat() - to.getLat());
		double longitudeArc = Math.toRadians(from.getLng() - to.getLng());

		double latitudeH = Math.sin(latitudeArc * 0.5);
		latitudeH *= latitudeH;
		double lontitudeH = Math.sin(longitudeArc * 0.5);
		lontitudeH *= lontitudeH;

		double tmp = Math.cos(Math.toRadians(from.getLat()))
				* Math.cos(Math.toRadians(to.getLat()));
		return Math.toDegrees(2.0 * Math.asin(Math.sqrt(latitudeH + tmp * lontitudeH)));
	}

	/**
	 * Computes the distance between two coordinates
	 * 
	 * @return distance in meters
	 */
	public static Length getDistance(Coord2D from, Coord2D to) {
		return new Length(RADIUS_OF_EARTH * Math.toRadians(getArcInRadians(from, to)));
	}

	/**
	 * Computes the distance between two coordinates taking in account the
	 * height difference
	 * 
	 * @return distance in meters
	 */
	public static Length get3DDistance(Coord3D end, Coord3D start) {
		Length horizontalDistance = getDistance(end, start);
		Length altitudeDiff = new Length(Math.abs((end.getAltitude().valueInMeters() - start
				.getAltitude().valueInMeters())));
		return MathUtil.hypot(horizontalDistance, altitudeDiff);
	}

	/**
	 * Computes the heading between two coordinates
	 * 
	 * @return heading in degrees
	 */
	public static double getHeadingFromCoordinates(Coord2D fromLoc, Coord2D toLoc) {
		double fLat = Math.toRadians(fromLoc.getLat());
		double fLng = Math.toRadians(fromLoc.getLng());
		double tLat = Math.toRadians(toLoc.getLat());
		double tLng = Math.toRadians(toLoc.getLng());

		double degree = Math.toDegrees(Math.atan2(
				Math.sin(tLng - fLng) * Math.cos(tLat),
				Math.cos(fLat) * Math.sin(tLat) - Math.sin(fLat) * Math.cos(tLat)
						* Math.cos(tLng - fLng)));

		return warpToPositiveAngle(degree);
	}

	public static double warpToPositiveAngle(double degree) {
		if (degree >= 0) {
			return degree;
		} else {
			return 360 + degree;
		}
	}

	/**
	 * Copied from android-map-utils (licensed under Apache v2)
	 * com.google.maps.android.SphericalUtil.java
	 * 
	 * @return area in m�
	 */
	public static Area getArea(Polygon poly) {
		List<Coord2D> path = poly.getPoints();
		int size = path.size();
		if (size < 3) {
			return new Area(0);
		}
		double total = 0;
		Coord2D prev = path.get(size - 1);
		double prevTanLat = tan((PI / 2 - toRadians(prev.getLat())) / 2);
		double prevLng = toRadians(prev.getLng());
		// For each edge, accumulate the signed area of the triangle formed by
		// the North Pole
		// and that edge ("polar triangle").
		for (Coord2D point : path) {
			double tanLat = tan((PI / 2 - toRadians(point.getLat())) / 2);
			double lng = toRadians(point.getLng());
			total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
			prevTanLat = tanLat;
			prevLng = lng;
		}
		return new Area(abs(total * (RADIUS_OF_EARTH * RADIUS_OF_EARTH)));
	}

	/**
	 * Copied from android-map-utils (licensed under Apache v2)
	 * com.google.maps.android.SphericalUtil.java
	 * 
	 * Returns the signed area of a triangle which has North Pole as a vertex.
	 * Formula derived from
	 * "Area of a spherical triangle given two edges and the included angle" as
	 * per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
	 * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71 The arguments
	 * named "tan" are tan((pi/2 - latitude)/2).
	 */
	private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
		double deltaLng = lng1 - lng2;
		double t = tan1 * tan2;
		return 2 * atan2(t * sin(deltaLng), 1 + t * cos(deltaLng));
	}

	public static Coord2D pointAlongTheLine(Coord2D start, Coord2D end, int distance) {
		return newCoordFromBearingAndDistance(start, getHeadingFromCoordinates(start, end), distance);
	}
}
