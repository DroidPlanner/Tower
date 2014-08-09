package org.droidplanner.android.utils.file;

import java.io.File;

import android.os.Environment;

public class DirectoryPath {

	/**
	 * Main path used to store files related to the program
	 * 
	 * @return Path to DroidPlanner/ folder in external storage
	 */
	static public String getDroidPlannerPath() {
		String root = Environment.getExternalStorageDirectory().getPath();
		return (root + "/DroidPlanner/");
	}

	/**
	 * Storage folder for Parameters
	 */
	static public String getParametersPath() {
		return getDroidPlannerPath() + "/Parameters/";
	}

	/**
	 * Storage folder for mission files
	 */
	static public String getWaypointsPath() {
		return getDroidPlannerPath() + "/Waypoints/";
	}

	/**
	 * Folder where telemetry log files are stored
	 */
	static public File getTLogPath() {
		File f = new File(getDroidPlannerPath() + "/Logs/");
		f.mkdirs();
		return f;
	}

	/**
	 * After tlogs are uploaded they get moved to this directory
	 */
	static public File getSentPath() {
		File f = new File(getTLogPath() + "/Sent/");
		f.mkdirs();
		return f;
	}

	/**
	 * Storage folder for user map tiles
	 */
	static public String getMapsPath() {
		return getDroidPlannerPath() + "/Maps/";
	}

	/**
	 * Storage folder for user camera description files
	 */
	public static String getCameraInfoPath() {
		return getDroidPlannerPath() + "/CameraInfo/";
	}

	/**
	 * Storage folder for stacktraces
	 */
	public static String getLogCatPath() {
		return getDroidPlannerPath() + "/LogCat/";
	}

	/**
	 * Storage folder for SRTM data
	 */
	static public String getSrtmPath() {
		return getDroidPlannerPath() + "/Srtm/";
	}

}
