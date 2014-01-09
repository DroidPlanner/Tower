package org.droidplanner.fragments.mission.survey;

import org.droidplanner.drone.variables.mission.survey.SurveyData;
import org.droidplanner.file.help.CameraInfoLoader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SurveyFragment extends Fragment {
	
	private Context context;
	private SurveyData surveyData;
	private CameraInfoLoader avaliableCameras;
	private SurveyViews views;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();
		surveyData = new SurveyData();
		views = new SurveyViews(context, surveyData);
		views.build(inflater, container,this);		
		avaliableCameras = new CameraInfoLoader(context);
		views.updateCameraSpinner(avaliableCameras.getCameraInfoList());
		return views.getLayout();
	}

	public  SurveyData getSurveyData() {
		return surveyData;
	}

	public boolean isFootPrintOverlayEnabled() {
		return views.footprintCheckBox.isChecked();
	}

}
