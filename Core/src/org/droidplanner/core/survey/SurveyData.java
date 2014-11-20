package org.droidplanner.core.survey;

import java.util.Locale;

import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Area;
import org.droidplanner.core.helpers.units.Length;

public class SurveyData {
	private CameraInfo camera = new CameraInfo();
	private Altitude altitude;
	private Double angle;
	private Double overlap;
	private Double sidelap;
	private Footprint footprint;

	public SurveyData(){
		update(0, new Altitude(50.0), 50, 60);
	}
	
	public void update(double angle, Altitude altitude, double overlap, double sidelap) {
		this.angle = angle;
		this.overlap = overlap;
		this.sidelap = sidelap;
		setAltitude(altitude);
	}

	public void setAltitude(Altitude altitude) {
		this.altitude = altitude;
		this.footprint = new Footprint(camera, this.altitude);	
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

	public Length getLongitudinalPictureDistance() {
		return new Length(getLongitudinalFootPrint().valueInMeters() * (1 - overlap * .01));
	}

	public Length getLateralPictureDistance() {
		return new Length(getLateralFootPrint().valueInMeters() * (1 - sidelap * .01));
	}

	public Altitude getAltitude() {
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

	public Length getLateralFootPrint() {
		return footprint.getLateralSize();
	}

	public Length getLongitudinalFootPrint() {
		return footprint.getLongitudinalSize();
	}

	public Area getGroundResolution() {
		return new Area(footprint.getGSD()*0.01);
	}

	public String getCameraName() {
		return camera.name;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "Altitude: %f Angle %f Overlap: %f Sidelap: %f", altitude,
				angle, overlap, sidelap);
	}

}