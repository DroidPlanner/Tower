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
	 * Return a filename that is suitable for a tlog
	 * 
	 * @return
	 * @throws FileNotFoundException
	 */
	static public File getTLogFile() {
		File myDir = DirectoryPath.getTLogPath();

		// We add a suffix .tmp to note incomplete tlogs
		return new File(myDir, FileManager.getTimeStamp() + ".tlog.tmp");
	}

	/**
	 * Get a buffered outputstream for a file
	 * 
	 * @return output file stream for the log file
	 */
	static public BufferedOutputStream openOutputStream(File filename)
			throws FileNotFoundException {
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(filename));
		return out;
	}

	/**
	 * If the specified file ends with .tmp, remove that suffix.
	 */
	public static void commitFile(File f) {
		String fullname = f.getAbsolutePath();
		if (f.exists() && fullname.endsWith(".tmp")) {
			String newname = fullname.substring(0, fullname.length() - 4);
			f.renameTo(new File(newname));
		}
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
