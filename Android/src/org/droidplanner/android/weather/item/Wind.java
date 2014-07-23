package org.droidplanner.android.weather.item;

public class Wind implements IWeatherItem {

	private double speed;

	public Wind(double speed) {
		this.speed = speed;
	}

	public double getSpeed() {
		return this.speed;
	}

}
