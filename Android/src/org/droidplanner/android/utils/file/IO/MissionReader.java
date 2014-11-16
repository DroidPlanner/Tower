package org.droidplanner.android.utils.file.IO;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;

import com.o3dr.services.android.lib.drone.mission.Mission;

/**
 * Read a mission from a file.
 */
public class MissionReader implements OpenFileDialog.FileReader {

    private static final String TAG = MissionReader.class.getSimpleName();

	private Mission mission = new Mission();

	public boolean openMission(String file) {
		if (!FileManager.isExternalStorageAvailable()) {
			return false;
		}
		try {
			final FileInputStream in = new FileInputStream(file);
            final ObjectInputStream objectIn = new ObjectInputStream(in);

            this.mission = (Mission) objectIn.readObject();
            objectIn.close();
			in.close();

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return false;
		}

		return true;
	}

	public Mission getMission() {
		return mission;
	}

	@Override
	public String getPath() {
		return DirectoryPath.getWaypointsPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getWaypointFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openMission(file);
	}
}
