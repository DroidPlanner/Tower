package com.droidplanner.survey;

import java.util.Locale;

import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.helpers.units.Area;
import com.droidplanner.helpers.units.Length;

public class SurveyData {
	private Double altitude;
	private Double angle;
	private Double overlap = 50.0;
	private Double sidelap = 60.0;
	private boolean generateInnerWps = false;
	private CameraInfo camera = new CameraInfo();

	public SurveyData(double defaultHatchAngle, double defaultAltitude) {
		this.angle = defaultHatchAngle;
		this.altitude = defaultAltitude;
	}

	public void update(double angle, double altitude, double overlap,
			double sidelap) {
		this.angle = angle;
		this.altitude = altitude;
		this.overlap = overlap;
		this.sidelap = sidelap;
	}

	public Length getLateralFootPrint() {
		return new Length(altitude * camera.getSensorLateralSize() / camera.focalLength);

	}

	public Length getLongitudinalFootPrint() {
		return new Length(altitude * camera.getSensorLongitudinalSize()
				/ camera.focalLength);
	}

	public Area getGroundResolution() {
		return new Area(((altitude
				* camera.getSensorLateralSize()
				/ camera.focalLength
				* (altitude * camera.getSensorLongitudinalSize() / camera.focalLength)
				/ (camera.sensorResolution * 1000)))/10000);
	}

	public Length getLongitudinalPictureDistance() {
		return new Length(getLongitudinalFootPrint().valueInMeters() * (1 - overlap * .01));
	}

	public Length getLateralPictureDistance() {
		return new Length(getLateralFootPrint().valueInMeters() * (1 - sidelap * .01));
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

	public void setInnerWpsState(boolean state) {
		generateInnerWps = state;
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

	public boolean shouldGenerateInnerWPs() {
		return generateInnerWps;
	}
	
	@Override
	public String toString() {
		return String.format(Locale.US,
				"Altitude: %f Angle %f Overlap: %f Sidelap: %f", altitude,
				angle, overlap, sidelap);
	}

}