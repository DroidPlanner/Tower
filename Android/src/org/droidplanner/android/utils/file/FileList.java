package org.droidplanner.android.utils.file;

import java.io.File;
import java.io.FilenameFilter;

public class FileList {

	static public String[] getWaypointFileList() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.contains(".txt");
			}
		};
		return getFileList(DirectoryPath.getWaypointsPath(), filter);
	}

	public static String[] getParametersFileList() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.contains(".param");
			}
		};
		return getFileList(DirectoryPath.getParametersPath(), filter);
	}

	static public String[] getKMZFileList() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.contains(".kml") || filename.contains(".kmz");
			}
		};
		return getFileList(DirectoryPath.getGCPPath(), filter);
	}

	public static String[] getCameraInfoFileList() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.contains(".xml");
			}
		};
		return getFileList(DirectoryPath.getCameraInfoPath(), filter);
	}

	static public String[] getFileList(String path, FilenameFilter filter) {
		File mPath = new File(path);
		try {
			mPath.mkdirs();
			if (mPath.exists()) {
				return mPath.list(filter);
			}
		} catch (SecurityException e) {
		}
		return new String[0];
	}

}
