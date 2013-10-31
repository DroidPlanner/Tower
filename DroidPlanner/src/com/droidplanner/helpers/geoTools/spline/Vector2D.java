package com.droidplanner.helpers.geoTools.spline;

import com.google.android.gms.maps.model.LatLng;

public class Vector2D {
	double x;
	double y;

	public Vector2D(Vector2D vector) {
		this(vector.x, vector.y);
	}
	

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D() {
		this(0, 0);
	}

	public Vector2D(LatLng latLng) {
		this(latLng.latitude,latLng.longitude);
	}
	
	public LatLng getCoordinate(){
		return new LatLng(x, y);
	}

	public Vector2D sum(Vector2D vector) {
		return new Vector2D(vector.x + this.x, vector.y + this.y);
	}

	public Vector2D subtract(Vector2D vector) {
		return new Vector2D(vector.x - this.x, vector.y - this.y);
	}

	public Vector2D dot(double scalar) {
		return new Vector2D(x * scalar, y * scalar);
	}

	public static Vector2D sum(Vector2D... toBeAdded) {
		Vector2D result = new Vector2D();
		for (Vector2D vector : toBeAdded) {
			result = result.sum(vector);
		}
		return result;
	}
}
