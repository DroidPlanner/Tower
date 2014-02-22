package org.droidplanner.helpers.coordinates;

public class Coord2D {
	private double x; // aka Longitude
	private double y; // aka Latitude

	public Coord2D(double x, double y) {
		set(x, y);
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

}
