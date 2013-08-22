import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class SrtmData {
	public String path;
	public int[][] data;

	public SrtmData(int[][] data) {
		this.data = data;
	}

	public boolean load(SRTM srtm, int lon, int lat) {
		// loads SRTM data for the lon,lat
		String fname = SRTM.getName(lon, lat);
		String region = SRTM.findRegion(fname, path);
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
		if (!(srtmFile.exists() || srtmZipFile.exists() || srtm.download(fname))) {
			// SRTMS.SRTMS.put(100 * lon + lat, null);
			return false;
		}
		if (srtmFile.exists()) {
			try {
				s = new FileInputStream(srtmFile);
			} catch (FileNotFoundException ex) {
				Logger.getLogger(SRTM.class.getName())
						.log(Level.SEVERE, "", ex);
				return false;
			}
		} else { // try zip file
			try {
				// try zip file
				ZipFile zf = new ZipFile(srtmZipFile);
				ZipEntry entry = zf.getEntry(fname);
				s = zf.getInputStream(entry);
	
			} catch (Exception ex) {
				Logger.getLogger(SRTM.class.getName())
						.log(Level.SEVERE, "", ex);
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
					Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE,
							"", ex);
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
}