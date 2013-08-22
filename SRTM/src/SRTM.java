/*
 * #%L
 * Osm2garminAPI
 * %%
 * Copyright (C) 2011 Frantisek Mantlik <frantisek at mantlik.cz>
 * %%
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author fm
 */
public class SRTM {
	private static final String url = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	private static final String[] REGIONS = {"Eurasia", "Africa", "Australia", "Islands",
		"North_America", "South_America"};

	private String path;
    public int[][] data = new int[1201][1201];
    private static final Map<String, Integer> regionMap = new HashMap<String, Integer>();

    SRTM(String dir) {
        this.path = dir;
    }

    public boolean load(int lon, int lat) {
// loads SRTM data for the lon,lat
        String fname = getName(lon, lat);
        String region = findRegion(fname, path);
        if (region == null) {
            return false;
        }
        File srtmFile;
        File srtmZipFile;
        if (!path.equals("")) {
            srtmFile = new File(path + "/" + region + "/" + fname);
            srtmZipFile = new File(path + "/" + region + "/" + fname + ".zip");
        } else {
            srtmFile = new File(fname);
            srtmZipFile = new File(fname + ".zip");
        }
        InputStream s;
        if (srtmZipFile.exists()) {
            try {
                // try zip file
                ZipFile zf = new ZipFile(srtmZipFile);
                ZipEntry entry = zf.getEntry(fname);
                s = zf.getInputStream(entry);
            } catch (IOException ex) {
                // broken download, try again
                srtmZipFile.delete();
            }
        }
        if (!(srtmFile.exists() || srtmZipFile.exists() || download(fname))) {
            //SRTMS.SRTMS.put(100 * lon + lat, null);
            return false;
        }
        if (srtmFile.exists()) {
            try {
                s = new FileInputStream(srtmFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
                return false;
            }
        } else { // try zip file
            try {
                // try zip file
                ZipFile zf = new ZipFile(srtmZipFile);
                ZipEntry entry = zf.getEntry(fname);
                s = zf.getInputStream(entry);

            } catch (Exception ex) {
                Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
                return false;
            }
        }
        int i = 0;
        while (i <= 1200) {
            int j = 0;
            while (j <= 1200) {
                try {
                    data[1200 - i][j] = 256 * s.read() + s.read();
                } catch (IOException ex) {
                    Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
                    return false;
                }
                j++;
            }
            i++;
        }
        try {
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
            return false;
        }
        return true;
    }

    public static boolean exists(int lon, int lat, String dir) {
        String fname = getName(-51, -29);
        String region = findRegion(fname, dir);
        return (region != null);
    }

    public static SRTM get(int lon, int lat, String dir) {
        SRTM srtm = new SRTM(dir);
        if (srtm.load(lon, lat)) {
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
        return srtm.data[i][j];
    }

	/*
	 * Returns region name for a file
	 */
	private static String findRegion(String fname, String srtmPath) {
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
	                if (!downloadRegionIndex(i, srtmPath, url)) {
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
	                                String srtm = line.substring(index, index + 7);
	                                regionMap.put(srtm, i);
	                            }
	                        }
	                    }
	                }
	                scanner.close();
	            } catch (FileNotFoundException ex) {
	                Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, null, ex);
	            }
	        }
	        System.out.println("SRTM map filled in with " + regionMap.size() + " entries.");
	    }
	    String name = fname.replace(".hgt", "");
	    if (regionMap.containsKey(name)) {
	        return REGIONS[regionMap.get(name)];
	    }
	    return null;
	}

	private static String getName(int lon, int lat) {
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

	private static boolean downloadRegionIndex(int region, String srtmPath, String url) {
	    String regionIndex = REGIONS[region] + ".index.html";
	    if (!srtmPath.equals("")) {
	        regionIndex = srtmPath + "/" + regionIndex;
	    }
	    File regionIndexFile = new File(regionIndex);
	    return downloadFile(url + REGIONS[region] + "/", regionIndexFile);
	}

	private static boolean downloadFile(String urlAddress, File output) {
	    URL url1;
	    InputStream inputs;
	    BufferedOutputStream outputs;
	    try {
	        url1 = new URL(urlAddress);
	    } catch (MalformedURLException ex) {
	        Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
	        return false;
	    }
	    try {
	        inputs = url1.openStream();
	        outputs = new BufferedOutputStream(new FileOutputStream(output));
	        int i = 0;
	        int ch = 0;
	        while (ch >= 0) {
	            ch = inputs.read();
	            if (ch >= 0) {
	                outputs.write(ch);
	            }
	            i++;
	            if (i % 100000 == 0) {
	                System.out.print("-");
	            }
	        }
	        System.out.println("");
	        inputs.close();
	        outputs.close();
	
	    } catch (FileNotFoundException ex) {
	        return false;
	    } catch (IOException ex) {
	        Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
	        return false;
	    }
	
	    return true;
	}

	private boolean download(String fname) {
	    File output;
	    String region = findRegion(fname, path);
	    if (region == null) {
	        return false;
	    }
	    if (path.equals("")) {
	        output = new File(region + "/" + fname + ".zip");
	    } else {
	        output = new File(path + "/" + region + "/" + fname + ".zip");
	    }
	    boolean result = downloadFile(url + region
	            + "/" + fname + ".zip", output);
	    // fix SRTM 2.1 naming problem in North America
	    if ((!result) && fname.startsWith("N5") && region.equalsIgnoreCase("North_America")) {
	        if (downloadFile(url + region
	                + "/" + fname.replace(".hgt", "hgt") + ".zip", output)) {
	            return true;
	        }
	    }
	    return result;
	}
}