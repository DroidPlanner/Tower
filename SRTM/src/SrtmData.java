import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SrtmData {
	public String path;
	public int[][] data = new int[1201][1201];
	File srtmFile;
	File srtmZipFile;

	public SrtmData() {
	}

	public boolean load(SRTM srtm, int lon, int lat) {
		// loads SRTM data for the lon,lat
		String fname = SRTM.getName(lon, lat);
		String region = SRTM.findRegion(fname, path);

		if (region == null) {
			return false;
		}

		setupFilePaths(fname, region);

		ZipFile zf;
		InputStream s;

		if (srtmZipFile.exists()) {
			try {
				// try zip file
				zf = new ZipFile(srtmZipFile);
				ZipEntry entry = zf.getEntry(fname);
				s = zf.getInputStream(entry);
				zf.close();
			} catch (IOException ex) {
				// broken download, try again
				srtmZipFile.delete();
			}
		}
		if (!(srtmFile.exists() || srtmZipFile.exists() || srtm.download(fname))) {
			return false;
		}
		try {
			if (srtmFile.exists()) {
				s = new FileInputStream(srtmFile);
			} else {// try zip file
				zf = new ZipFile(srtmZipFile);
				ZipEntry entry = zf.getEntry(fname);
				s = zf.getInputStream(entry);
			}
			readHtgFile(s);
			s.close();
		} catch (Exception ex) {
			Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
			return false;
		}
		return true;
	}

	private void setupFilePaths(String fname, String region) {
		if (!path.equals("")) {
			srtmFile = new File(path + "/" + region + "/" + fname);
			srtmZipFile = new File(path + "/" + region + "/" + fname + ".zip");
		} else {
			srtmFile = new File(fname);
			srtmZipFile = new File(fname + ".zip");
		}
	}

	private void readHtgFile(InputStream s) throws IOException {
		int i = 0;
		while (i <= 1200) {
			int j = 0;
			while (j <= 1200) {
				data[1200 - i][j] = 256 * s.read() + s.read();
				j++;
			}
			i++;
		}
	}
}