
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class SrtmData {
	public String path;
	File srtmFile;
	File srtmZipFile;
	ZipFile zf;
	BufferedInputStream s;

	public int load(SRTM srtm, double lon, double lat) throws Exception {
		int altitude;
		
		// loads SRTM data for the lon,lat
		String fname = SRTM.getName(lon, lat);
		String region = SrtmRegions.findRegion(fname, path);

		if (region == null) {
			throw new Exception("Null Region");
		}

		setupFilePaths(fname, region);

		if (loadSrtmFile(srtm, fname) == false) {
			throw new Exception("Failed to load SRTM file");
		}
		
		openSrtmFile(fname);
		altitude = readHtgFile(s,lon,lat);
		s.close();
		return altitude;
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

	private int readHtgFile(BufferedInputStream s, double lon, double lat) throws Exception {
		
		byte[] buffer = new byte[2];

		int ai = (int) Math.round(1200d * (lat - Math.floor(lat)));
		int aj = (int) Math.round(1200d * (lon - Math.floor(lon)));
		int index = (aj+(1200-ai)*1201)*2;
		
		if(s.skip(index)!=index){
			throw new Exception("error when skipping");
		}
		s.read(buffer);
		

		return ByteBuffer.wrap(buffer).order(
				ByteOrder.BIG_ENDIAN).getShort();
	}
}