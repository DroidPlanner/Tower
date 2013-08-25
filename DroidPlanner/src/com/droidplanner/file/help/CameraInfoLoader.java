package com.droidplanner.file.help;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;
import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.file.IO.CameraInfoReader;

public class CameraInfoLoader {

	private static final String CAMERA_INFO_ASSESTS_FOLDER = "CameraInfo/";
	private Context context;
	private HashMap<String, String> filesInSdCard = new HashMap<String, String>();
	private HashMap<String, String> filesInAssets = new HashMap<String, String>();

	public CameraInfoLoader(Context context) {
		this.context = context;
	}

	public CameraInfo openFile(String file) throws Exception {
		CameraInfoReader reader = new CameraInfoReader();
		if (filesInSdCard.containsKey(file)) {
			Log.d("", "SD File");						
			reader.openFile(new FileInputStream(filesInSdCard.get(file)));
			return reader.getCameraInfo();
		}else if (filesInAssets.containsKey(file)) {
			Log.d("", "assetFile");
			reader.openFile(context.getAssets().open(filesInAssets.get(file)));
		}
		throw new FileNotFoundException();
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
			String[] list = context.getAssets().list("CameraInfo");
			filesInAssets.clear();
			for (String string : list) {
				filesInAssets.put(string, CAMERA_INFO_ASSESTS_FOLDER+string);
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
			filesInSdCard.put(string, DirectoryPath.getCameraInfoPath()+string);
		}
		return list;
	}
}