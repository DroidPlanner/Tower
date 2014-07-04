package org.droidplanner.android.utils.file.IO;

import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Read tlog file w/ optional message filter
 *
 * <timestamp><MavLink packet>...
 *
 * See http://qgroundcontrol.org/mavlink for details
 *
 */
public class TLogReader implements OpenFileDialog.FileReader {

    public boolean openTLog(String file) {
		if (!FileManager.isExternalStorageAvaliable()) {
			return false;
		}
		try {
			final FileInputStream in = new FileInputStream(file);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			// TODO

			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public String getPath() {
		return DirectoryPath.getLogPath();
	}

	@Override
	public String[] getFileList() {
		return FileList.getTLogFileList();
	}

	@Override
	public boolean openFile(String file) {
		return openTLog(file);
	}
}
