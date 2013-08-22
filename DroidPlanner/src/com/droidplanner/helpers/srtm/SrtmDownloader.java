package com.droidplanner.helpers.srtm;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SrtmDownloader {

	static void downloadRegionIndex(int region, String srtmPath, String url)
			throws IOException {
		String regionIndex = SrtmRegions.REGIONS[region] + ".index.html";
		if (!srtmPath.equals("")) {
			regionIndex = srtmPath + "/" + regionIndex;
		}
		File regionIndexFile = new File(regionIndex);
		downloadFile(url + SrtmRegions.REGIONS[region] + "/", regionIndexFile);
	}

	static void downloadSrtmFile(String fname, String path) throws Exception {
		File output;
		String region = SrtmRegions.findRegion(fname, path);
		output = new File(path + "/" + fname + ".zip");
		downloadSrtmFile(fname, output, region);
		UnZip.unZipIt(fname, output);
		output.delete();
	}

	private static void downloadSrtmFile(String fname, File output,
			String region) throws IOException {
		try {
			downloadFile(Srtm.url + region + "/" + fname + ".zip", output);
		} catch (IOException e) {
			downloadAlternativeSrtmFile(fname, output, region, e);
		}
	}

	private static void downloadAlternativeSrtmFile(String fname, File output,
			String region, IOException e) throws IOException {
		// fix SRTM 2.1 naming problem in North America
		if (fname.startsWith("N5")
				&& region.equalsIgnoreCase("North_America")) {
			downloadFile(
					Srtm.url + region + "/" + fname.replace(".hgt", "hgt")
							+ ".zip", output);
		} else {
			throw e;
		}
	}

	private static void downloadFile(String urlAddress, File output)
			throws IOException {
		URL url1;
		InputStream inputs;
		BufferedOutputStream outputs;
		System.out.println("file:" + output.getName());
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

	}

}
