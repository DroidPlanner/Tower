package org.droidplanner.android.helpers;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileStream;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

/**
 * Title provider for a MapView from the local storage. Based on:
 * http://stackoverflow.com/questions/14784841/tileprovider-using-local-tiles
 * 
 */
public class LocalMapTileProvider implements TileProvider {
	private static final int TILE_WIDTH = 256;
	private static final int TILE_HEIGHT = 256;
	private static final int BUFFER_SIZE = 16 * 1024;

	public LocalMapTileProvider() {
		tryToAddANoMediaFile();
	}

	private void tryToAddANoMediaFile() {
		try {
			FileStream.createNoMediaFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Tile getTile(int x, int y, int zoom) {
		byte[] image = readTileImage(x, y, zoom);
		if (image == null) {
			return NO_TILE;
		} else {
			return new Tile(TILE_WIDTH, TILE_HEIGHT, image);
		}

	}

	private byte[] readTileImage(int x, int y, int zoom) {
		FileInputStream in = null;
		ByteArrayOutputStream buffer = null;

		try {
			String patch = DirectoryPath.getMapsPath()
					+ getTileFilename(x, y, zoom);
			in = new FileInputStream(patch);
			buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[BUFFER_SIZE];

			while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();

			return buffer.toByteArray();
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception ignored) {
				}
			if (buffer != null)
				try {
					buffer.close();
				} catch (Exception ignored) {
				}
		}
	}

	private String getTileFilename(int x, int y, int zoom) {
		return String.format(Locale.US, "%d/%d/%d.jpg", zoom, y, x);
	}

}