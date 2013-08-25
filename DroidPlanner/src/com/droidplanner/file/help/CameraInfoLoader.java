package com.droidplanner.file.help;

import java.io.IOException;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.droidplanner.file.FileList;

public class CameraInfoLoader {
	public ArrayAdapter<CharSequence> avaliableCameras;
	private Context context;

	public CameraInfoLoader(Context context) {
		this.context = context;
	}

	String[] getCameraInfoListFromAssets() {
		try {
			return context.getAssets().list("CameraInfo");
		} catch (IOException e) {
			return new String[0];
		}
	}

	public String[] getCameraInfoListFromStorage() {
		String[] list = FileList.getCameraInfoFileList();
		return list;
	}
}