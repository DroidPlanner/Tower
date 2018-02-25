package org.droidplanner.services.android.impl.utils.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DirectoryPath {

	/**
	 * Main path used to store private data files related to the program
	 * 
	 * @return Path to DroneKit-Android private data folder in external storage
	 */
	static public String getPrivateDataPath(Context context) {
        File dataDir = context.getExternalFilesDir(null);
        return dataDir.getAbsolutePath();
	}

    /**
     * Main path used to store public data files related to the app.
     * @param context application context
     * @return Path to DroneKit-Android public data directory.
     */
    public static String getPublicDataPath(Context context){
        final String root = Environment.getExternalStorageDirectory().getPath();
        return root + "/3DRServices/";
    }

	/**
	 * Storage folder for user camera description files
	 */
	public static String getCameraInfoPath(Context context) {
		return getPublicDataPath(context) + "/CameraInfo/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getCrashLogPath(Context context) {
		return getPrivateDataPath(context) + "/crash_log/";
	}

}
