package org.droidplanner.core.srtm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.droidplanner.core.srtm.Srtm.OnProgressListner;

public class SrtmDownloader {
	static final String url = "http://dds.cr.usgs.gov/srtm/version2_1/SRTM3/";
	private OnProgressListner listner;

	public SrtmDownloader(OnProgressListner listner) {
		this.listner = listner;
	}

	public void downloadRegionIndex(int region, String srtmPath) throws IOException {
		String regionIndex = SrtmRegions.REGIONS[region] + ".index.html";
		regionIndex = getIndexPath(srtmPath) + regionIndex;
		File regionIndexFile = new File(regionIndex);
		downloadFile(url + SrtmRegions.REGIONS[region] + "/", regionIndexFile);
	}

	public void downloadSrtmFile(String fname, String path) throws Exception {
		File output;
		String region = new SrtmRegions(path).findRegion(fname, listner);
		output = new File(path + "/" + fname + ".zip");
		downloadSrtmFile(fname, output, region);
		UnZip.unZipIt(fname, output);
		output.delete();
	}

	private void downloadSrtmFile(String fname, File output, String region) throws IOException {
		try {
			downloadFile(SrtmDownloader.url + region + "/" + fname + ".zip", output);
		} catch (IOException e) {
			downloadAlternativeSrtmFile(fname, output, region, e);
		}
	}

	private void downloadAlternativeSrtmFile(String fname, File output, String region, IOException e)
			throws IOException {
		// fix SRTM 2.1 naming problem in North America
		if (fname.startsWith("N5") && region.equalsIgnoreCase("North_America")) {
			downloadFile(SrtmDownloader.url + region + "/" + fname.replace(".hgt", "hgt") + ".zip",
					output);
		} else {
			throw e;
		}
	}

	private void downloadFile(String urlAddress, File file) throws IOException {
		URL url = new URL(urlAddress);
		URLConnection connection = url.openConnection();
		connection.connect();
		// this will be useful so that you can show a typical 0-100% progress
		// bar
		long fileLength = connection.getContentLength();

		// download the file
		InputStream input = new BufferedInputStream(url.openStream());
		BufferedOutputStream outputs = new BufferedOutputStream(new FileOutputStream(file));

		byte data[] = new byte[2048];
		long total = 0;
		int count;
		while ((count = input.read(data)) != -1) {
			total += count;
			outputs.write(data, 0, count);
			callListner(file.getName(), (int) (total * 100 / fileLength));
		}

		outputs.flush();
		outputs.close();
		input.close();
	}

	private void callListner(String filename, int i) {
		if (listner != null) {
			if (i >= 0) {
				listner.onProgress(filename, i);
			} else {
				listner.onProgress(filename, -1);
			}
		}
	}

	public static String getIndexPath(String srtmPath) {
		return srtmPath + "/Index/";
	}

}
