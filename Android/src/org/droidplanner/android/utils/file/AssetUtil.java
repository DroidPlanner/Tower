package org.droidplanner.android.utils.file;

import java.io.IOException;

import android.content.res.AssetManager;

public class AssetUtil {

	public static boolean exists(AssetManager assetManager, String directory,
			String fileName) throws IOException {
		final String[] assets = assetManager.list(directory);
		for (String asset : assets)
			if (asset.equals(fileName))
				return true;
		return false;
	}
}
