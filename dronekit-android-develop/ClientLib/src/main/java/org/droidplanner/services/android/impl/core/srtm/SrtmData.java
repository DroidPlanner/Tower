package org.droidplanner.services.android.impl.core.srtm;

import org.droidplanner.services.android.impl.core.srtm.Srtm.OnProgressListner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SrtmData {
	private String path;
	private File srtmFile;
	private BufferedInputStream s;

	public SrtmData(String dir) {
		path = dir;
	}

	public int load(double lon, double lat, OnProgressListner listner) throws Exception {
		int altitude;

		String fname = SrtmData.getName(lon, lat);
		setupFilePaths(fname);
		downloadSrtmFileIfNeeded(fname, listner);

		s = new BufferedInputStream(new FileInputStream(srtmFile));
		altitude = readHtgFile(s, lon, lat);
		s.close();
		return altitude;
	}

	private void downloadSrtmFileIfNeeded(String fname, OnProgressListner listner) throws Exception {
		if (!srtmFile.exists()) {
			new SrtmDownloader(listner).downloadSrtmFile(fname, path);
		}
	}

	private void setupFilePaths(String fname) {
		srtmFile = new File(path + "/" + fname);
	}

	private int readHtgFile(BufferedInputStream s, double lon, double lat) throws Exception {
		byte[] buffer = new byte[2];
		int index = calculateFileIndex(lon, lat);
		skipToDataPositionInFile(index);
		s.read(buffer);
		return ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN).getShort();
	}

	private void skipToDataPositionInFile(int index) throws Exception {
		if (s.skip(index) != index) {
			throw new Exception("error when skipping");
		}
	}

	private int calculateFileIndex(double lon, double lat) {
		int ai = (int) Math.round(1200d * (lat - Math.floor(lat)));
		int aj = (int) Math.round(1200d * (lon - Math.floor(lon)));
		int index = (aj + (1200 - ai) * 1201) * 2;
		return index;
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