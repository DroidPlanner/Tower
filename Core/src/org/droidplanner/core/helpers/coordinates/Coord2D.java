package org.droidplanner.core.helpers.coordinates;

public class Coord2D {
	private double latitude;  // aka x
	private double longitude; // aka y

	public Coord2D(double lat, double lon) {
		set(lat, lon);
	}

	public Coord2D(Coord2D point) {
		set(point);
	}

	public void set(double lat, double lon) {
		this.latitude = lat;
		this.longitude = lon;
	}

	public void set(Coord2D coord) {
		set(coord.latitude, coord.longitude);
	}

	public double getX() {
		return latitude;
	}

	public double getY() {
		return longitude;
	}

	public double getLng() {
		return longitude;
	}

	public double getLat() {
		return latitude;
	}

	@Override
	public String toString() {
		return "lat/lon: " + getLat() + "/" + getLng();
	}

}
