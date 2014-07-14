package org.droidplanner.android.maps.weather.provider.items;

public class Wind implements WeatherItem {

	private double speed;
	private double bearing;

	public Wind(double speed, double bearing) {
		this.speed = speed;
		this.bearing = bearing;
	}

	public double getSpeed() {
		return this.speed;
	}

	public double getBearing() {
		return this.bearing;
	}

}
