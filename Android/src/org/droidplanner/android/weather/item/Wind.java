package org.droidplanner.android.weather.item;

public class Wind implements IWeatherItem {

	private double speed;
	public static int MEDIUM_SPEED = 5;
	public static int EXTREME_SPEED = 10;

	public Wind(double speed) {
		this.speed = speed;
	}

	public double getSpeed() {
		return this.speed;
	}

}
