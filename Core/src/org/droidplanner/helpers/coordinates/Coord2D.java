package org.droidplanner.helpers.coordinates;

public class Coord2D {
	private double x; // aka Latitude
	private double y; // aka Longitude

	public Coord2D(int x, double y) {
		set(x, y);
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
