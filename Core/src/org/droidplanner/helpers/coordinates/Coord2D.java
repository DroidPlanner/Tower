package org.droidplanner.helpers.coordinates;

public class Coord2D {
	private double x; // aka Latitude
	private double y; // aka Longitude

	public Coord2D(double x2, double y) {
		set(x2, y);
	}

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
