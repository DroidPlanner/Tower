import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SrtmRegions {
	static final String[] REGIONS = { "Eurasia", "Africa", "Australia",
	"Islands", "North_America", "South_America" };
	static final Map<String, Integer> regionMap = new HashMap<String, Integer>();

	/*
	 * Returns region name for a file
	 */
	static String findRegion(String fname, String srtmPath) {
		if (SrtmRegions.regionMap.isEmpty()) {
			if (!SrtmRegions.fillRegionData(srtmPath)) {
				return null;
			}
			System.out.println("SRTM map filled in with " + SrtmRegions.regionMap.size()
					+ " entries.");
	
		}
		String name = fname.replace(".hgt", "");
		if (SrtmRegions.regionMap.containsKey(name)) {
			return SrtmRegions.REGIONS[SrtmRegions.regionMap.get(name)];
		}
		return null;
	}

	static boolean fillRegionData(String srtmPath) {
		System.err.println("Downloading SRTM map data.");
		String region;
		for (int i = 0; i < SrtmRegions.REGIONS.length; i++) {
			region = SrtmRegions.REGIONS[i];
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
				if (!SrtmDownloader.downloadRegionIndex(i, srtmPath, SRTM.url)) {
					// download error, try again with the next attempt
					SrtmRegions.regionMap.clear();
					return false;
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
							SrtmRegions.regionMap.put(srtm, i);
						} else {
							index = line.indexOf("hgt.zip") - 7;
							if (index >= 0) {
								String srtm = line.substring(index,
										index + 7);
								SrtmRegions.regionMap.put(srtm, i);
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
		return true;
	}
}