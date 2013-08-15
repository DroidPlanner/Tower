package com.droidplanner.survey;

import java.util.Locale;

import com.droidplanner.file.IO.CameraInfo;

public class SurveyData {
	private Double altitude;
	private Double angle;
	private Double overlap;
	private Double sidelap;
	private CameraInfo camera = new CameraInfo();
	private Double sensorFpLat;
	private Double sensorFpLong;
	private Double sensorFpRes;
	private Double separationLat;    //This (lateral) distance between grid lines
	private Double separationLong;   // The (longitudinal) distance between pictures

	public SurveyData(double defaultHatchAngle, double defaultAltitude) {
		this.angle = 90.0;
		this.altitude = 150.0;
		this.overlap = 50.0;
		this.sidelap = 60.0;
	}

	public void setCameraInfo(CameraInfo info) {
		this.camera=info;
		tryToLoadOverlapFromCamera();
	}

	private void tryToLoadOverlapFromCamera() {
		if (camera.overlap!=null) {
			this.overlap = camera.overlap;
		}
		if (camera.sidelap!=null) {
			this.sidelap = camera.sidelap;
		}
	}

	public void update(double angle, double altitude, double overlap,
			double sidelap) {
		this.angle = angle;
		this.altitude = altitude;
		this.overlap = overlap;
		this.sidelap = sidelap;
		update();
	}
	
	private void update() {
		double focalLength = camera.focalLength;
		Double sensorResolution = camera.sensorResolution;
		Double sensorLat = camera.getSensorLateralSize();
		Double sensorLong = camera.getSensorLongitudinalSize();

		sensorFpLat = altitude*sensorLat/focalLength;
		sensorFpLong = altitude*sensorLong/focalLength;
		sensorFpRes = sensorFpLat*sensorFpLong/(sensorResolution*1000);
		separationLong = sensorFpLong*(1-overlap*.01);
		separationLat = sensorFpLat*(1-sidelap*.01);
		
	}

	public Double getDistanceBetweenPictures() {
		update();
		return this.separationLong;
	}

	public double getLateralFootPrint() {
		update();
		return this.sensorFpLat;
	}

	public double getLongitudinalFootPrint() {
		update();
		return this.sensorFpLong;
	}

	public double getGroundResolution() {
		update();
		return this.sensorFpRes;
	}

	public Double getLineDistance() {
		update();
		return this.separationLat;
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