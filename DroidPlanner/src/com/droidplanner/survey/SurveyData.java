package com.droidplanner.survey;

import java.util.Locale;

import com.droidplanner.file.IO.CameraInfo;

public class SurveyData {
	private Double altitude;
	private Double angle;
	private Double overlap;
	private Double sidelap;
	private CameraInfo camera = new CameraInfo();

	public SurveyData(double defaultHatchAngle, double defaultAltitude) {
		this.angle = 90.0;
		this.altitude = 150.0;
		this.overlap = 50.0;
		this.sidelap = 60.0;
	}

	public void update(double angle, double altitude, double overlap,
			double sidelap) {
		this.angle = angle;
		this.altitude = altitude;
		this.overlap = overlap;
		this.sidelap = sidelap;
	}

	public double getLateralFootPrint() {
		return altitude * camera.getSensorLateralSize() / camera.focalLength;

	}

	public double getLongitudinalFootPrint() {
		return altitude * camera.getSensorLongitudinalSize()
				/ camera.focalLength;
	}

	public double getGroundResolution() {
		return altitude
				* camera.getSensorLateralSize()
				/ camera.focalLength
				* (altitude * camera.getSensorLongitudinalSize() / camera.focalLength)
				/ (camera.sensorResolution * 1000);
	}

	public Double getLongitudinalPictureDistance() {
		return getLongitudinalFootPrint() * (1 - overlap * .01);
	}

	public Double getLateralPictureDistance() {
		return getLateralFootPrint() * (1 - sidelap * .01);
	}

	public void setCameraInfo(CameraInfo info) {
		this.camera = info;
		tryToLoadOverlapFromCamera();
	}

	private void tryToLoadOverlapFromCamera() {
		if (camera.overlap != null) {
			this.overlap = camera.overlap;
		}
		if (camera.sidelap != null) {
			this.sidelap = camera.sidelap;
		}
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
		return String.format(Locale.US,
				"Altitude: %f Angle %f Overlap: %f Sidelap: %f", altitude,
				angle, overlap, sidelap);
	}

}