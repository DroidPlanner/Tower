package com.droidplanner.file.IO;

public class CameraInfo {

	public Double imageWidth;
	public Double imageHeight;
	public Double focalLength;
	public Double overlap;
	public Double sidelap;
	public Object isInLandscapeOrientation = true;
	public double sensorResolution;

	@Override
	public String toString() {
		return "ImageWidth:" + imageWidth + " ImageHeight:" + imageHeight
				+ " FocalLength:" + focalLength + " Overlap:" + overlap
				+ " Sidelap:" + sidelap + " isInLandscapeOrientation:"
				+ isInLandscapeOrientation;

	}
}