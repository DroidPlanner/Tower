package org.droidplanner.android.utils.file;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

public class FileManager {

	/**
	 * Timestamp for logs in the Mission Planner Format
	 */
	static public String getTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
		String timeStamp = sdf.format(new Date());
		return timeStamp;
	}

	public static boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

}
