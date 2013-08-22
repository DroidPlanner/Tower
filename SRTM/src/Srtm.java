/*
 * #%L
 * Osm2garminAPI
 * %%
 * Copyright (C) 2011 Frantisek Mantlik <frantisek at mantlik.cz>
 * %%
 */


/**
 * 
 * @author fm
 */
public class Srtm {
	static final String url = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	public SrtmData srtmData = new SrtmData();
	public SrtmRegions regions = new SrtmRegions();
	
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

	static String getName(double Dlon, double Dlat) {

		int lon = (int) Math.floor(Dlon);
		int lat = (int) Math.floor(Dlat);
		
		String dirlat = "N";
		if (lat < 0) {
			dirlat = "S";
		}
		String dirlon = "E";
		if (lon < 0) {
			dirlon = "W";
		}
		String st = String.valueOf(Math.abs(lat));
		while (st.length() < 2) {
			st = "0" + st;
		}
		String fname = dirlat + st;
		st = String.valueOf(Math.abs(lon));
		while (st.length() < 3) {
			st = "0" + st;
		}
		fname = fname + dirlon + st + ".hgt";
		return fname;
	}
}