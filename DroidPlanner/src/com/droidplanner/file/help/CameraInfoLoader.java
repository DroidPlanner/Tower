package com.droidplanner.file.help;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;
import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.file.IO.CameraInfoReader;

public class CameraInfoLoader {

	private static final String CAMERA_INFO_ASSESTS_FOLDER = "CameraInfo";
	private Context context;
	private HashMap<String, String> filesInSdCard = new HashMap<String, String>();
	private HashMap<String, String> filesInAssets = new HashMap<String, String>();

	public CameraInfoLoader(Context context) {
		this.context = context;
	}

	public CameraInfo openFile(String file) throws Exception {
		if (filesInSdCard.containsKey(file)) {
			return readSdCardFile(file);
		} else if (filesInAssets.containsKey(file)) {
			return readAssetsFile(file);
		} else {
			throw new FileNotFoundException();
		}
	}

	private CameraInfo readSdCardFile(String file) throws Exception {
		CameraInfoReader reader = new CameraInfoReader();
		InputStream inputStream = new FileInputStream(filesInSdCard.get(file));
		reader.openFile(inputStream);
		inputStream.close();
		return reader.getCameraInfo();
	}

	private CameraInfo readAssetsFile(String file) throws Exception {
		CameraInfoReader reader = new CameraInfoReader();
		InputStream inputStream = context.getAssets().open(
				filesInAssets.get(file));
		reader.openFile(inputStream);
		inputStream.close();
		return reader.getCameraInfo();
	}

	public SpinnerAdapter getCameraInfoList() {
		ArrayAdapter<CharSequence> avaliableCameras = new ArrayAdapter<CharSequence>(
				context, android.R.layout.simple_spinner_dropdown_item);
		avaliableCameras.addAll(getCameraInfoListFromStorage());
		avaliableCameras.addAll(getCameraInfoListFromAssets());
		return avaliableCameras;
	}

	private String[] getCameraInfoListFromAssets() {
		try {
			String[] list = context.getAssets()
					.list(CAMERA_INFO_ASSESTS_FOLDER);
			filesInAssets.clear();
			for (String string : list) {
				filesInAssets.put(string, CAMERA_INFO_ASSESTS_FOLDER + "/"
						+ string);
			}
			return list;

		} catch (IOException e) {
			return new String[0];
		}
	}

	private String[] getCameraInfoListFromStorage() {
		String[] list = FileList.getCameraInfoFileList();
		filesInSdCard.clear();
		for (String string : list) {
			filesInSdCard.put(string, DirectoryPath.getCameraInfoPath()
					+ string);
		}
		return list;
	}
}