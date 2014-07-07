package org.droidplanner.core.srtm;

public class Srtm {
	private static final int SRTM_NaN = -32768;

	/**
	 * Callback for progress reports
	 */
	public interface OnProgressListner {
		public void onProgress(String filename, int percentage);
	}

	private SrtmData srtmData;
	private OnProgressListner listner;

	/**
	 * @param directory
	 *            Cache directory
	 */
	public Srtm(String directory) {
		srtmData = new SrtmData(directory);
	}

	/**
	 * Get SRTM elevation for geographic coordinate (WGS-84)
	 * 
	 * Stores a cache of uncompressed SRTM data files at the default directory.
	 * It need a Internet connection to fetch SRTM files if they are not in the
	 * disk
	 * 
	 * @return Above Sea Level (ASL) altitude in meters
	 */
	public int getData(double longitude, double latitude) {
		try {
			return srtmData.load(longitude, latitude, listner);
		} catch (Exception e) {
			e.printStackTrace();
			return SRTM_NaN; // SRTM NaN
		}
	}

	/**
	 * If a file needs to be download this listener will be called periodically
	 */
	public void setListner(OnProgressListner listner) {
		this.listner = listner;
	}
}