public class Srtm {
	private static final int SRTM_NaN = -32768;
	public SrtmData srtmData = new SrtmData();
	
	Srtm(String dir) {
		this.srtmData.path = dir;
	}

	/*
	 * Get SRTM elevation in meters for lon and lat WGS-84 coordinates
	 */
	public static int getData(double lon, double lat, String dir) {
		try {
			return get(lon, lat, dir);
		} catch (Exception e) {
			e.printStackTrace();
			return SRTM_NaN; // SRTM NaN
		}
	}
	
	private static int get(double lon, double lat, String dir) throws Exception {
		Srtm srtm = new Srtm(dir);
		return srtm.srtmData.load(srtm, lon, lat);
	}
}