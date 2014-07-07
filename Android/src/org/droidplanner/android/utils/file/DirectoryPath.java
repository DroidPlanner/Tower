package org.droidplanner.android.utils.file;

import java.io.File;

import android.os.Environment;

public class DirectoryPath {

	static public String getDroidPlannerPath() {
		String root = Environment.getExternalStorageDirectory().getPath();
		return (root + "/DroidPlanner/");
	}

	static public String getParametersPath() {
		return getDroidPlannerPath() + "/Parameters/";
	}

	static public String getWaypointsPath() {
		return getDroidPlannerPath() + "/Waypoints/";
	}

	static public String getGCPPath() {
		return getDroidPlannerPath() + "/GCP/";
	}

	static public File getTLogPath() {
		File f = new File(getDroidPlannerPath() + "/Logs/");
		f.mkdirs();
		return f;
	}

	/**
	 * After tlogs are uploaded they get moved to this directory
	 * 
	 * @return
	 */
	static public File getSentPath() {
		File f = new File(getDroidPlannerPath() + "/Sent/");
		f.mkdirs();
		return f;
	}

	static public String getMapsPath() {
		return getDroidPlannerPath() + "/Maps/";
	}

	public static String getCameraInfoPath() {
		return getDroidPlannerPath() + "/CameraInfo/";
	}

	public static String getLogCatPath() {
		return getDroidPlannerPath() + "/LogCat/";
	}

	static public String getSrtmPath() {
		return getDroidPlannerPath() + "/Srtm/";
	}

}
