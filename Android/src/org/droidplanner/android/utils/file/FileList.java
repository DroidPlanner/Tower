package org.droidplanner.android.utils.file;

import java.io.File;
import java.io.FilenameFilter;

public class FileList {

    public static final String WAYPOINT_FILENAME_EXT = ".dpwp";

    public static final String PARAM_FILENAME_EXT = ".param";

    public static final String CAMERA_FILENAME_EXT = ".xml";

    public static final String TLOG_FILENAME_EXT = ".tlog";

	static public String[] getWaypointFileList() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(WAYPOINT_FILENAME_EXT);
			}
		};
		return getFileList(DirectoryPath.getWaypointsPath(), filter);
	}

	public static String[] getParametersFileList() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(PARAM_FILENAME_EXT);
			}
		};
		return getFileList(DirectoryPath.getParametersPath(), filter);
	}

	public static String[] getCameraInfoFileList() {
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(CAMERA_FILENAME_EXT);
			}
		};
		return getFileList(DirectoryPath.getCameraInfoPath(), filter);
	}

    public static String[] getTLogFileList() {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.contains(TLOG_FILENAME_EXT);
            }
        };
        return getFileList(DirectoryPath.getTLogPath().getPath(), filter);
    }

	static public String[] getFileList(String path, FilenameFilter filter) {
		File mPath = new File(path);
		try {
			mPath.mkdirs();
			if (mPath.exists()) {
				return mPath.list(filter);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

}
