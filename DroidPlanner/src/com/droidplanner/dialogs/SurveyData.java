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

	public void update(double angle, double altitude, double overlap, double sidelap) {
		
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