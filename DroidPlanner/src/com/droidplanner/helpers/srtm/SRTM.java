package com.droidplanner.helpers.srtm;
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
public class SRTM {
	static final String url = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	public SrtmData srtmData = new SrtmData();
	public SrtmRegions regions = new SrtmRegions();
	
	SRTM(String dir) {
		this.srtmData.path = dir;
	}

	public static SRTM get(int lon, int lat, String dir) {
		SRTM srtm = new SRTM(dir);
		if (srtm.srtmData.load(srtm, lon, lat)) {
			return srtm;
		}
		return null;
	}

	/*
	 * Get SRTM elevation in meters for lon and lat WGS-84 coordinates
	 */
	public static int getData(double lon, double lat, String dir) {
		SRTM srtm = get((int) Math.floor(lon), (int) Math.floor(lat), dir);
		int i = (int) Math.round(1200d * (lat - Math.floor(lat)));
		int j = (int) Math.round(1200d * (lon - Math.floor(lon)));
		if (srtm == null) {
			return -32768; // SRTM NaN
		}
		return srtm.srtmData.data[i][j];
	}

	static String getName(int lon, int lat) {
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