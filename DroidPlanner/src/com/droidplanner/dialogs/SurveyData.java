package com.droidplanner.dialogs;

public class SurveyData {
	private Double altitude;
	private Double lineDistance;
	private Double angle;
	private Double overlap;
	private Double sidelap;

	public SurveyData(double defaultHatchAngle, double defaultAltitude, double overlap, double sidelap) {
		this.angle = defaultHatchAngle;
		this.altitude = defaultAltitude;
		this.overlap = overlap;
		this.sidelap = sidelap;		
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public Double getLineDistance() {
		return lineDistance;
	}

	public void setLineDistance(Double lineDistance) {
		this.lineDistance = lineDistance;
	}

	public Double getAngle() {
		return angle;
	}

	public void setAngle(Double angle) {
		this.angle = angle;
	}

	public Double getOverlap() {
		return overlap;
	}

	public void setOverlap(Double overlap) {
		this.overlap = overlap;
	}

	public Double getSidelap() {
		return sidelap;
	}

	public void setSidelap(Double sidelap) {
		this.sidelap = sidelap;
	}
}