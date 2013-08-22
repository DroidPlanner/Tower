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

	static boolean downloadRegionIndex(int region, String srtmPath,
			String url) {
		String regionIndex = SrtmRegions.REGIONS[region] + ".index.html";
		if (!srtmPath.equals("")) {
			regionIndex = srtmPath + "/" + regionIndex;
		}
		File regionIndexFile = new File(regionIndex);
		return downloadFile(url + SrtmRegions.REGIONS[region] + "/", regionIndexFile);
	}

	static boolean downloadSrtmFile(String fname, String path) throws Exception {
		boolean result;
		File output;
		String region = SrtmRegions.findRegion(fname, path);
		output = new File(path +"/" + fname + ".zip");
		result = downloadZipedSrtmFile(fname, output, region);
		UnZip.unZipIt(fname,output);
		output.delete();
		return result;
	}

	private static boolean downloadZipedSrtmFile(String fname, File output,
			String region) {
		boolean result;
		result = downloadFile(SRTM.url + region + "/" + fname + ".zip", output);
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

	private static boolean downloadFile(String urlAddress, File output) {
		URL url1;
		InputStream inputs;
		BufferedOutputStream outputs;
		System.out.println("file:"+output.getName());
		try {
			url1 = new URL(urlAddress);
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
	
		} catch (MalformedURLException ex) {
			Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
			return false;
		} catch (FileNotFoundException ex) {
			return false;
		} catch (IOException ex) {
			Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
			return false;
		}
	
		return true;
	}

}
