package org.droidplanner.android.utils.file.help;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.IO.CameraInfoReader;
import org.droidplanner.core.mission.survey.CameraInfo;

import android.content.Context;

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

	public List<String> getCameraInfoList() {
		ArrayList<String> avaliableCameras = new ArrayList<String>();
		List<String> cameraInfoListFromStorage = getCameraInfoListFromStorage();
		avaliableCameras.addAll(cameraInfoListFromStorage);
		List<String> cameraInfoListFromAssets = getCameraInfoListFromAssets();
		avaliableCameras.addAll(cameraInfoListFromAssets);
		return avaliableCameras;
	}

	private List<String> getCameraInfoListFromAssets() {
		try {
			String[] list = context.getAssets()
					.list(CAMERA_INFO_ASSESTS_FOLDER);
			filesInAssets.clear();
			for (String string : list) {
				filesInAssets.put(string, CAMERA_INFO_ASSESTS_FOLDER + "/"
						+ string);
			}
			return Arrays.asList(list);

		} catch (IOException e) {
			return new ArrayList<String>();
		}
	}

	private List<String> getCameraInfoListFromStorage() {
		List<String> list = Arrays.asList(FileList.getCameraInfoFileList());
		filesInSdCard.clear();
		for (String string : list) {
			filesInSdCard.put(string, DirectoryPath.getCameraInfoPath()
					+ string);
		}
		return list;
	}
}