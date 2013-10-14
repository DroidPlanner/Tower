package com.droidplanner.file;

import android.os.Environment;

public class DirectoryPath {

    public static final String PARAMETER_METADATA_XML = "ParameterMetaData.xml";

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

	static public String getTLogPath() {
		return getDroidPlannerPath() + "/Logs/";
	}

	static public String getMapsPath() {
		return getDroidPlannerPath() + "/Maps/";
	}

	public static String getCameraInfoPath() {
		return getDroidPlannerPath() + "/CameraInfo/";
	}

    public static String getParameterMetadataPath() {
        return getParametersPath() + "/" + PARAMETER_METADATA_XML;
    }
}
