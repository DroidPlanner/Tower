/*
 * #%L
 * Osm2garminAPI
 * %%
 * Copyright (C) 2011 Frantisek Mantlik <frantisek at mantlik.cz>
 * %%
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author fm
 */
public class SRTM {
	private static final String url = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	static final String[] REGIONS = { "Eurasia", "Africa", "Australia",
			"Islands", "North_America", "South_America" };

	public SrtmData srtmData = new SrtmData(new int[1201][1201]);
	private static final Map<String, Integer> regionMap = new HashMap<String, Integer>();

	SRTM(String dir) {
		this.srtmData.path = dir;
	}

	public static boolean exists(int lon, int lat, String dir) {
		String fname = getName(-51, -29);
		String region = findRegion(fname, dir);
		return (region != null);
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

	/*
	 * Returns region name for a file
	 */
	static String findRegion(String fname, String srtmPath) {
		if (regionMap.isEmpty()) {
			System.err.println("Downloading SRTM map data.");
			String region;
			for (int i = 0; i < REGIONS.length; i++) {
				region = REGIONS[i];
				String indexPath = region;
				if (!srtmPath.equals("")) {
					indexPath = srtmPath + "/" + indexPath;
				}
				File indexDir = new File(indexPath);
				if (!indexDir.exists()) {
					indexDir.mkdirs();
				}
				indexPath += ".index.html";
				File indexFile = new File(indexPath);
				if (!indexFile.exists()) {
					if (!SrtmDownloader.downloadRegionIndex(i, srtmPath, url)) {
						// download error, try again with the next attempt
						regionMap.clear();
						return null;
					}
				}
				try {
					Scanner scanner = new Scanner(indexFile);
					while (scanner.hasNext()) {
						String line = scanner.next();
						if (line.contains("href=\"")) {
							int index = line.indexOf(".hgt.zip") - 7;
							if (index >= 0) {
								String srtm = line.substring(index, index + 7);
								regionMap.put(srtm, i);
							} else {
								index = line.indexOf("hgt.zip") - 7;
								if (index >= 0) {
									String srtm = line.substring(index,
											index + 7);
									regionMap.put(srtm, i);
								}
							}
						}
					}
					scanner.close();
				} catch (FileNotFoundException ex) {
					Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE,
							null, ex);
				}
			}
			System.out.println("SRTM map filled in with " + regionMap.size()
					+ " entries.");
		}
		String name = fname.replace(".hgt", "");
		if (regionMap.containsKey(name)) {
			return REGIONS[regionMap.get(name)];
		}
		return null;
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

	boolean download(String fname) {
		File output;
		String region = findRegion(fname, srtmData.path);
		if (region == null) {
			return false;
		}
		if (srtmData.path.equals("")) {
			output = new File(region + "/" + fname + ".zip");
		} else {
			output = new File(srtmData.path + "/" + region + "/" + fname + ".zip");
		}
		boolean result = SrtmDownloader.downloadFile(url + region + "/" + fname + ".zip",
				output);
		// fix SRTM 2.1 naming problem in North America
		if ((!result) && fname.startsWith("N5")
				&& region.equalsIgnoreCase("North_America")) {
			if (SrtmDownloader.downloadFile(url + region + "/" + fname.replace(".hgt", "hgt")
					+ ".zip", output)) {
				return true;
			}
		}
		return result;
	}
}