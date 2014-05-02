package org.droidplanner.core.mission.survey;

public class CameraInfo {
	public String name = "Default";
	public Double sensorWidth = 6.12;
	public Double sensorHeight = 4.22;
	public Double focalLength = 7.0;
	public Double overlap = 50.0;
	public Double sidelap = 60.0;
	public boolean isInLandscapeOrientation = true;
	public Double sensorResolution = 10.1;

	public Double getSensorLateralSize() {
		if (isInLandscapeOrientation) {
			return sensorWidth;
		} else {
			return sensorHeight;
		}
	}

	public Double getSensorLongitudinalSize() {
		if (isInLandscapeOrientation) {
			return sensorHeight;
		} else {
			return sensorWidth;
		}
	}

	@Override
	public String toString() {
		return "ImageWidth:" + sensorWidth + " ImageHeight:" + sensorHeight
				+ " FocalLength:" + focalLength + " Overlap:" + overlap
				+ " Sidelap:" + sidelap + " isInLandscapeOrientation:"
				+ isInLandscapeOrientation;

	}

}