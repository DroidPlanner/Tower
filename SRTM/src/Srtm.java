public class Srtm {
	public SrtmData srtmData = new SrtmData();
	
	Srtm(String dir) {
		this.srtmData.path = dir;
	}

	public static int get(double lon, double lat, String dir) throws Exception {
		Srtm srtm = new Srtm(dir);
		return srtm.srtmData.load(srtm, lon, lat);
	}

	/*
	 * Get SRTM elevation in meters for lon and lat WGS-84 coordinates
	 */
	public static int getData(double lon, double lat, String dir) {
		try {
			return get(lon, lat, dir);
		} catch (Exception e) {
			e.printStackTrace();
			return -32768; // SRTM NaN
		}
	}
}