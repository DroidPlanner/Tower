
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SrtmData {
	public String path;
	File srtmFile;
	BufferedInputStream s;

	public int load(Srtm srtm, double lon, double lat) throws Exception {
		int altitude;
		
		// loads SRTM data for the lon,lat
		String fname = Srtm.getName(lon, lat);
		String region = SrtmRegions.findRegion(fname, path);

		setupFilePaths(fname, region);

		loadSrtmFile(srtm, fname);
		
		s = new BufferedInputStream(new FileInputStream(srtmFile));
		altitude = readHtgFile(s,lon,lat);
		s.close();
		return altitude;
	}

	private void loadSrtmFile(Srtm srtm, String fname) throws Exception  {
		if (srtmFile.exists()) {
			return;
		}
		SrtmDownloader.downloadSrtmFile(fname, path);
	}

	private void setupFilePaths(String fname, String region) {
			srtmFile = new File(path + "/" + fname);
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