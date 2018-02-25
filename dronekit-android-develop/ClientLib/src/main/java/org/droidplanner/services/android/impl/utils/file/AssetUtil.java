package org.droidplanner.services.android.impl.utils.file;

import android.content.res.AssetManager;

import java.io.IOException;

public class AssetUtil {

	public static boolean exists(AssetManager assetManager, String directory, String fileName)
			throws IOException {
		final String[] assets = assetManager.list(directory);
		for (String asset : assets)
			if (asset.equals(fileName))
				return true;
		return false;
	}
}
