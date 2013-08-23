package com.srtm;

public class Srtm {
	private static final int SRTM_NaN = -32768;

	public interface OnProgressListner {
		public void onProgress(float percentage, String filename);
	}

	private SrtmData srtmData;

	/**
	 * @param directory Cache directory
	 */
	public Srtm(String directory) {
		srtmData = new SrtmData(directory);
	}

	/**
	 * Get SRTM elevation for geographic coordinate
	 * (WGS-84)
	 * @return Above Sea Level (ASL) altitude in meters
	 */
	public int getData(double longitude, double latitude) {
		try {
			return srtmData.load(longitude, latitude);
		} catch (Exception e) {
			e.printStackTrace();
			return SRTM_NaN; // SRTM NaN
		}
	}
}