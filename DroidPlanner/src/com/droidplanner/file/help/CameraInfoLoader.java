package com.droidplanner.file.help;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.droidplanner.R.string;
import com.droidplanner.dialogs.SurveyDialog;
import com.droidplanner.file.DirectoryPath;
import com.droidplanner.file.FileList;
import com.droidplanner.file.IO.CameraInfoReader;

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

	public void openFile(SurveyDialog surveyDialog, String text) {
		String filenameWithPath = DirectoryPath.getCameraInfoPath() + text;
		FileInputStream in;
		try {
			in = new FileInputStream(filenameWithPath);
			CameraInfoReader reader = new CameraInfoReader();
			if (!reader.openFile(in)) {
				Toast.makeText(surveyDialog.context,
						surveyDialog.context.getString(string.error_when_opening_file),
						Toast.LENGTH_SHORT).show();
				surveyDialog.surveyData.setCameraInfo(reader.getNewMockCameraInfo());
			} else {
				surveyDialog.surveyData.setCameraInfo(reader.getCameraInfo());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}