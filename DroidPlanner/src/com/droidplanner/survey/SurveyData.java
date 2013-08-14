package com.droidplanner.survey;

import java.util.Locale;

import com.droidplanner.file.IO.CameraInfo;

public class SurveyData {
	private Double altitude;
	private Double angle;
	private Double overlap;
	private Double sidelap;
	private CameraInfo cameraInfo = new CameraInfo();
	private Double area;
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
		
		double imageHeight = cameraInfo.imageHeight = 4.22;
		double imageWidth = cameraInfo.imageWidth = 6.12;
		double focalLength = cameraInfo.focalLength = 7.0;
		Double sensorResolution = cameraInfo.sensorResolution = 10.1;
		Double sensorLat = imageWidth;
		Double sensorLong = imageHeight;

		if ((Boolean) cameraInfo.isInLandscapeOrientation)
		{
			sensorLat = imageWidth;
			sensorLong = imageHeight;
		}
		else
		{
			sensorLat = imageHeight;
			sensorLong = imageWidth;
		}

		this.sensorFpLat = this.altitude*sensorLat/focalLength;
		this.sensorFpLong = this.altitude*sensorLong/focalLength;
		this.sensorFpRes = this.sensorFpLat*this.sensorFpLong/(sensorResolution*1000);
		this.area = this.sensorFpLat*this.sensorFpLong;
		this.separationLong = this.sensorFpLong*(1-this.overlap*.01);
		this.separationLat = this.sensorFpLat*(1-sidelap*.01);
		
	}

//	public double getMissionLength() {
//		update(this.angle,this.altitude,this.overlap,this.sidelap);
//		return this.separationLat;
//	}

	public Double getArea() {
		update(this.angle,this.altitude,this.overlap,this.sidelap);
		return this.area;
	}

	public Double getDistanceBetweenPictures() {
		update(this.angle,this.altitude,this.overlap,this.sidelap);
		return this.separationLong;
	}

	public double getLateralFootPrint() {
		update(this.angle,this.altitude,this.overlap,this.sidelap);
		return this.sensorFpLat;
	}

	public double getLongitudinalFootPrint() {
		update(this.angle,this.altitude,this.overlap,this.sidelap);
		return this.sensorFpLong;
	}

	public double getGroundResolution() {
		update(this.angle,this.altitude,this.overlap,this.sidelap);
		return this.sensorFpRes;
	}

	public Double getLineDistance() {
		update(this.angle,this.altitude,this.overlap,this.sidelap);
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