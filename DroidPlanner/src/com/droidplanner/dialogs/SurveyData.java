package com.droidplanner.dialogs;

public class SurveyData {
	private Double altitude;
	private Double lineDistance;
	private Double angle;
	private Double overlap;
	private Double sidelap;

	public SurveyData(double defaultHatchAngle, double defaultAltitude) {
		this.angle = defaultHatchAngle;
		this.altitude = defaultAltitude;
		this.overlap = 50.0;
		this.sidelap = 60.0;
	}

	public void update(double angle, double altitude, double overlap,
			double sidelap) {
	}

	public double getMissionLength() {
		// TODO Do some calculation and return the correct value
		return 10;
	}

	public double getArea() {
		// TODO Do some calculation and return the correct value
		return 20;
	}

	public double getDistanceBetweenPictures() {
		// TODO Do some calculation and return the correct value
		return 30;
	}

	public double getLateralFootPrint() {
		// TODO Do some calculation and return the correct value
		return 40;
	}

	public double getLongitudinalFootPrint() {
		// TODO Do some calculation and return the correct value
		return 50;
	}

	public double getGroundResolution() {
		// TODO Do some calculation and return the correct value 
		return 3.4;
	}

	public Double getAltitude() {
		return altitude;
	}

	public Double getLineDistance() {
		return lineDistance;
	}

	public Double getAngle() {
		return angle;
	}

	public double getSidelap() {
		return sidelap;
	}

	public double getOverlap() {
		return overlap;
	}
}