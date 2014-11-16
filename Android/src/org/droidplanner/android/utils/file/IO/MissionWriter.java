package org.droidplanner.android.utils.file.IO;

import android.util.Log;

import com.o3dr.services.android.lib.drone.mission.Mission;

import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;
import org.droidplanner.android.utils.file.FileStream;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * Write a mission to file.
 */
public class MissionWriter {
	private static final String TAG = MissionWriter.class.getSimpleName();

	public static boolean write(Mission mission) {
		return write(mission, FileStream.getWaypointFilename("waypoints"));
	}

	public static boolean write(Mission mission, String filename) {
		try {
			if (!FileManager.isExternalStorageAvailable())
				return false;

			if (!filename.endsWith(FileList.WAYPOINT_FILENAME_EXT)) {
				filename += FileList.WAYPOINT_FILENAME_EXT;
			}

			final FileOutputStream out = FileStream.getWaypointFileStream(filename);
			final ObjectOutputStream objectOut = new ObjectOutputStream(out);
			objectOut.writeObject(mission);
			objectOut.close();
			out.close();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
		return true;
	}
}
