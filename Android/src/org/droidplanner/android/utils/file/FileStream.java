package org.droidplanner.android.utils.file;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileStream {
	public static FileOutputStream getParameterFileStream(String filename) throws
            FileNotFoundException {
		File myDir = new File(DirectoryPath.getParametersPath());
		myDir.mkdirs();
		File file = new File(myDir, filename);
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

    public static String getParameterFilename(String prefix){
        return prefix + "-" + getTimeStamp() + FileList.PARAM_FILENAME_EXT;
    }

	public static FileOutputStream getExceptionFileStream(Context context) throws FileNotFoundException {
		File myDir = new File(DirectoryPath.getCrashLogPath(context));
		myDir.mkdirs();
		File file = new File(myDir, getTimeStamp() + ".txt");
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

	static public FileOutputStream getWaypointFileStream(String filename) throws
            FileNotFoundException {
		File myDir = new File(DirectoryPath.getWaypointsPath());
		myDir.mkdirs();
		File file = new File(myDir, filename);
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

    public static String getWaypointFilename(String prefix){
        return prefix + "-" + getTimeStamp() + FileList.WAYPOINT_FILENAME_EXT;
    }

	/**
	 * Creates a new .nomedia file on the maps folder
	 * 
	 * It's used to hide the maps tiles from android gallery
	 * 
	 * @throws IOException
	 * 
	 */
	static public void createNoMediaFile() throws IOException {
		File myDir = new File(DirectoryPath.getMapsPath());
		myDir.mkdirs();
		new File(myDir, ".nomedia").createNewFile();
	}

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
