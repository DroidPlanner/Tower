import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SrtmDownloader {

	static boolean downloadFile(String urlAddress, File output) {
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

	static boolean downloadRegionIndex(int region, String srtmPath,
			String url) {
		String regionIndex = SRTM.REGIONS[region] + ".index.html";
		if (!srtmPath.equals("")) {
			regionIndex = srtmPath + "/" + regionIndex;
		}
		File regionIndexFile = new File(regionIndex);
		return downloadFile(url + SRTM.REGIONS[region] + "/", regionIndexFile);
	}

	static boolean download(String fname, String path) {
		File output;
		String region = SRTM.findRegion(fname, path);
		if (region == null) {
			return false;
		}
		if (path.equals("")) {
			output = new File(region + "/" + fname + ".zip");
		} else {
			output = new File(path + "/" + region + "/" + fname + ".zip");
		}
		boolean result = downloadFile(SRTM.url + region + "/" + fname + ".zip",
				output);
		// fix SRTM 2.1 naming problem in North America
		if ((!result) && fname.startsWith("N5")
				&& region.equalsIgnoreCase("North_America")) {
			if (downloadFile(SRTM.url + region + "/" + fname.replace(".hgt", "hgt")
					+ ".zip", output)) {
				return true;
			}
		}
		return result;
	}

}
