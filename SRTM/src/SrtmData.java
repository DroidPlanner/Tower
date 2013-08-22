import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class SrtmData {
	public String path;
	public int[][] data = new int[1201][1201];
	File srtmFile;
	File srtmZipFile;
	ZipFile zf;
	BufferedInputStream s;

	public boolean load(SRTM srtm, int lon, int lat) {
		// loads SRTM data for the lon,lat
		String fname = SRTM.getName(lon, lat);
		String region = SrtmRegions.findRegion(fname, path);

		if (region == null) {
			return false;
		}

		setupFilePaths(fname, region);

		if (loadSrtmFile(srtm, fname) == false) {
			return false;			
		}
		
		try {
			openSrtmFile(fname);
			readHtgFile(s);
			s.close();
		} catch (Exception ex) {
			Logger.getLogger(SRTM.class.getName()).log(Level.SEVERE, "", ex);
			return false;
		}
		return true;
	}

	private void openSrtmFile(String fname) throws FileNotFoundException,
			ZipException, IOException {
		if (srtmFile.exists()) {
			s = new BufferedInputStream(new FileInputStream(srtmFile));
		} else {// try zip file
			zf = new ZipFile(srtmZipFile);
			ZipEntry entry = zf.getEntry(fname);
			s = new BufferedInputStream(zf.getInputStream(entry));
		}
	}

	private boolean loadSrtmFile(SRTM srtm, String fname) {
		if (srtmZipFile.exists()) {
			try {
				// try zip file
				zf = new ZipFile(srtmZipFile);
				ZipEntry entry = zf.getEntry(fname);
				s =  new BufferedInputStream(zf.getInputStream(entry));
				zf.close();
			} catch (IOException ex) {
				// broken download, try again
				srtmZipFile.delete();
			}
		}
		if (!(srtmFile.exists() || srtmZipFile.exists() || SrtmDownloader.download(fname, path))) {
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

	private void readHtgFile(BufferedInputStream s) throws IOException {
		byte[] buffer = new byte[1201 * 1201 * 2];
		s.read(buffer);

		ByteBuffer intBuffer = ByteBuffer.wrap(buffer).order(
				ByteOrder.BIG_ENDIAN);

		int i = 0;
		while (i <= 1200) {
			int j = 0;
			while (j <= 1200) {
				data[1200 - i][j] = intBuffer.getShort();
				j++;
			}
			i++;
		}
	}
}