package com.srtm;

public class Srtm {
	private static final int SRTM_NaN = -32768;

	public interface OnProgressListner {
		public void onProgress(float percentage, String filename);
	}

	public SrtmData srtmData;

	public Srtm(String dir) {
		srtmData = new SrtmData(dir);
	}

	/*
	 * Get SRTM elevation in meters for lon and lat WGS-84 coordinates
	 */
	public int getData(double lon, double lat) {
		try {
			return srtmData.load(lon, lat);
		} catch (Exception e) {
			e.printStackTrace();
			return SRTM_NaN; // SRTM NaN
		}
	}
}