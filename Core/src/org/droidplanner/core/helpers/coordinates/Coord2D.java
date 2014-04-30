package org.droidplanner.core.helpers.coordinates;

public class Coord2D {
	private double x; // aka Longitude
	private double y; // aka Latitude

	public Coord2D(double x, double y) {
		set(x, y);
	}

	public Coord2D(Coord2D point) {
		set(point);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void set(Coord2D coord) {
		set(coord.x, coord.y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getLng() {
		return x;
	}

	public double getLat() {
		return y;
	}

	@Override
	public String toString() {
		return "X/Y: " + getX() + "/" + getY();
	}

}
