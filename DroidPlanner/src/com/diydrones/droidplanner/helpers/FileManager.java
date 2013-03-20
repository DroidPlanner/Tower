package com.diydrones.droidplanner.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

public class FileManager {

	static public String getDroidPlannerPath() {
		String root = Environment.getExternalStorageDirectory().toString();
		return (root + "/DroidPlanner/");
	}

	static public String getWaypointsPath() {
		return getDroidPlannerPath() + "/Waypoints/";
	}

	static public String getGCPPath() {
		return getDroidPlannerPath() + "/GCP/";
	}

	static private String getTLogPath() {
		return getDroidPlannerPath() + "/Logs/";
	}

	static public String getMapsPath() {
		return getDroidPlannerPath() + "/Maps/";
	}

	static public FileOutputStream getWaypointFileStream()
			throws FileNotFoundException {
		File myDir = new File(getWaypointsPath());
		myDir.mkdirs();
		File file = new File(myDir, "waypoints-" + getTimeStamp() + ".txt");
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

	static public String[] loadWaypointFileList() {
		File mPath = new File(getWaypointsPath());
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			return null;
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.contains(".txt");
				}
			};
			return mPath.list(filter);
		} else {
			return null;
		}

	}

	static public String[] loadKMZFileList() {
		File mPath = new File(getGCPPath());
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			return new String[0];
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.contains(".kml")
							|| filename.contains(".kmz");
				}
			};
			return mPath.list(filter);
		} else {
			return new String[0];
		}

	}

	/**
	 * Get a file Stream for logging purposes
	 * 
	 * @return output file stream for the log file
	 */
	static public BufferedOutputStream getTLogFileStream()
			throws FileNotFoundException {
		File myDir = new File(getTLogPath());
		myDir.mkdirs();
		File file = new File(myDir, getTimeStamp() + ".tlog");
		if (file.exists())
			file.delete();
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file));
		return out;
	}

	/**
	 * Timestamp for logs in the Mission Planner Format
	 */
	static private String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",
				Locale.US);
		String timeStamp = sdf.format(new Date());
		return timeStamp;
	}
	
	public static boolean isExternalStorageAvaliable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

}
