package com.droidplanner.survey;

import java.util.Locale;

import com.droidplanner.file.IO.CameraInfo;

public class SurveyData {
	private Double altitude;
	private Double angle;
	private Double overlap;
	private Double sidelap;
	private CameraInfo cameraInfo = new CameraInfo();

	public SurveyData(double defaultHatchAngle, double defaultAltitude) {
		this.angle = defaultHatchAngle;
		this.altitude = defaultAltitude;
		this.overlap = 50.0;
		this.sidelap = 50.0;
	}

	public void setCameraInfo(CameraInfo info) {
		this.cameraInfo=info;
		tryToLoadOverlapFromCamera();
	}

	private void tryToLoadOverlapFromCamera() {
		if (cameraInfo.overlap!=null) {
			this.overlap = cameraInfo.overlap;
		}
		if (cameraInfo.sidelap!=null) {
			this.sidelap = cameraInfo.sidelap;
		}
	}

	public void update(double angle, double altitude, double overlap,
			double sidelap) {
		this.angle = angle;
		this.altitude = altitude;
		this.overlap = overlap;
		this.sidelap = sidelap;
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
		return altitude;
	}

	public Double getLineDistance() {
		// TODO Do some calculation and return the correct value
		return 50.0;
	}

	public Double getAltitude() {
		return altitude;
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

	@Override
	public String toString() {
		return String.format(Locale.US,"Altitude: %f Angle %f Overlap: %f Sidelap: %f", altitude,angle,overlap,sidelap);
	}	
	

}