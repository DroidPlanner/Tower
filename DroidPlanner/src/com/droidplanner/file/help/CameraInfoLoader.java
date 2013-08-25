package com.droidplanner.file.help;

import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.droidplanner.R.string;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;
import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.file.IO.CameraInfoReader;

public class CameraInfoLoader {
	public ArrayAdapter<CharSequence> avaliableCameras;
	private Context context;

	public CameraInfoLoader(Context context) {
		this.context = context;
	}

	private String[] getCameraInfoListFromAssets() {
		try {
			return context.getAssets().list("CameraInfo");
		} catch (IOException e) {
			return new String[0];
		}
	}

	private String[] getCameraInfoListFromStorage() {
		String[] list = FileList.getCameraInfoFileList();
		return list;
	}

	public CameraInfo openFile(String text) {
		String filenameWithPath = DirectoryPath.getCameraInfoPath() + text;
		FileInputStream in;
		try {
			in = new FileInputStream(filenameWithPath);
			CameraInfoReader reader = new CameraInfoReader();
			reader.openFile(in);
			return reader.getCameraInfo();
		} catch (Exception e) {
			Toast.makeText(
					context,
					context
							.getString(string.error_when_opening_file),
					Toast.LENGTH_SHORT).show();
			return CameraInfoReader.getNewMockCameraInfo();
		}
	}

	public void rebuildCameraInfoList() {
		avaliableCameras = new ArrayAdapter<CharSequence>(context,
				android.R.layout.simple_spinner_dropdown_item);
		avaliableCameras.addAll(getCameraInfoListFromStorage());
		avaliableCameras.addAll(getCameraInfoListFromAssets());
	}
}