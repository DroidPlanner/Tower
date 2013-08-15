package com.droidplanner.file.IO;

public class CameraInfo {

	public Double imageWidth;
	public Double imageHeight;
	public Double focalLength;
	public Double overlap;
	public Double sidelap;
	public Boolean isInLandscapeOrientation = true;
	public double sensorResolution;

	public Double getSensorLateralSize() {
		if (isInLandscapeOrientation){
			return  imageWidth;
		}else{
			return imageHeight;
		}
	}
	
	public Double getSensorLongitudinalSize() {
		if (isInLandscapeOrientation){
			return  imageHeight;
		}else{
			return imageWidth;
		}
	}
	
	@Override
	public String toString() {
		return "ImageWidth:" + imageWidth + " ImageHeight:" + imageHeight
				+ " FocalLength:" + focalLength + " Overlap:" + overlap
				+ " Sidelap:" + sidelap + " isInLandscapeOrientation:"
				+ isInLandscapeOrientation;

	}

}