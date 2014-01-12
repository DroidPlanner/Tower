package org.droidplanner.file.IO;

public class CameraInfo {

	public Double sensorWidth;
	public Double sensorHeight;
	public Double focalLength;
	public Double overlap;
	public Double sidelap;
	public boolean isInLandscapeOrientation = true;
	public Double sensorResolution;

	public Double getSensorLateralSize() {
		if (isInLandscapeOrientation){
			return  sensorWidth;
		}else{
			return sensorHeight;
		}
	}
	
	public Double getSensorLongitudinalSize() {
		if (isInLandscapeOrientation){
			return  sensorHeight;
		}else{
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