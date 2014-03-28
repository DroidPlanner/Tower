package org.droidplanner.android.utils.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStream {
	public static FileOutputStream getParameterFileStream()
			throws FileNotFoundException {
		File myDir = new File(DirectoryPath.getParametersPath());
		myDir.mkdirs();
		File file = new File(myDir, "Parameters-" + FileManager.getTimeStamp()
				+ ".param");
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

	public static FileOutputStream getExceptionFileStream()
			throws FileNotFoundException {
		File myDir = new File(DirectoryPath.getLogCatPath());
		myDir.mkdirs();
		File file = new File(myDir, FileManager.getTimeStamp() + ".txt");
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

	static public FileOutputStream getWaypointFileStream(String name)
			throws FileNotFoundException {
		File myDir = new File(DirectoryPath.getWaypointsPath());
		myDir.mkdirs();
		File file = new File(myDir, name + "-" + FileManager.getTimeStamp()
				+ ".txt");
		if (file.exists())
			file.delete();
		FileOutputStream out = new FileOutputStream(file);
		return out;
	}

	/**
	 * Get a file Stream for logging purposes
	 * 
	 * @return output file stream for the log file
	 */
	static public BufferedOutputStream getTLogFileStream()
			throws FileNotFoundException {
		File myDir = new File(DirectoryPath.getTLogPath());
		myDir.mkdirs();
		File file = new File(myDir, FileManager.getTimeStamp() + ".tlog");
		if (file.exists())
			file.delete();
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file));
		return out;
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

}
