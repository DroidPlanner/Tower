package org.droidplanner.android.utils.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DirectoryPath {

    /**
     * Main path used to store private data files related to the program
     * @param context application context
     * @return Path to Tower private data directory.
     */
    public static String getPrivateDataPath(Context context){
        File dataDir = context.getExternalFilesDir(null);
        return dataDir.getAbsolutePath();
    }

	/**
	 * Main path used to store public data files related to the program
	 * 
	 * @return Path to Tower data directory in external storage
	 */
	static public String getPublicDataPath() {
		String root = Environment.getExternalStorageDirectory().getPath();
		return (root + "/Tower/");
	}

	/**
	 * Storage folder for Parameters
	 */
	static public String getParametersPath() {
		return getPublicDataPath() + "/Parameters/";
	}

	/**
	 * Storage folder for mission files
	 */
	static public String getWaypointsPath() {
		return getPublicDataPath() + "/Waypoints/";
	}

	/**
	 * Storage folder for user map tiles
	 */
	static public String getMapsPath() {
		return getPublicDataPath() + "/Maps/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getLogCatPath(Context context) {
		return getPrivateDataPath(context) + "/LogCat/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getCrashLogPath(Context context) {
		return getPrivateDataPath(context) + "/crash_log/";
	}
}
