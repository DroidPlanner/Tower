package com.droidplanner.fragments.survey;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.file.IO.CameraInfoReader;
import com.droidplanner.file.help.CameraInfoLoader;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.survey.SurveyData;
import com.droidplanner.survey.grid.Grid;
import com.droidplanner.survey.grid.GridBuilder;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public class SurveyFragment extends Fragment implements 
		OnTextSeekBarChangedListner, OnSpinnerItemSelectedListener, OnClickListener {
	//public abstract void onPolygonGenerated(List<waypoint> list);

	public interface OnNewGridListner{
		public void onNewGrid(Grid grid);
		public void onClearPolygon();
	}
	
	public enum pathModes {
		MISSION, POLYGON;
	}
	
	private Context context;
	private Polygon polygon;

	private SurveyData surveyData;
	private CameraInfoLoader avaliableCameras;
	private SurveyViews views;
	private Grid grid;
	
	private OnNewGridListner onSurveyListner;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();
		views = new SurveyViews(context);
		views.build(inflater, container,this);
		
		surveyData = new SurveyData();
		avaliableCameras = new CameraInfoLoader(context);
		views.updateCameraSpinner(avaliableCameras.getCameraInfoList());
		
		return views.getLayout();
	}

	public void setSurveyData(Polygon polygon, double defaultAltitude){
		this.polygon = polygon;		
		surveyData.setAltitude(defaultAltitude);
		update();
	}
	
	public void setOnSurveyListner(OnNewGridListner listner){
		this.onSurveyListner = listner;
	}

	
	@Override
	public void onSeekBarChanged() {		
		generateGrid();						
	}

	public void generateGrid() {
		surveyData.update(views.angleView.getValue(), views.altitudeView.getValue(),
				views.overlapView.getValue(), views.sidelapView.getValue());
		//TODO find a better origin point than (0,0)
		
		try {
			GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLng(0, 0));
			checkIfPolygonIsValid(polygon);
			grid = gridBuilder.generate();
			views.updateViews(surveyData,grid,polygon.getArea());
			grid.setAltitude(surveyData.getAltitude());
			onSurveyListner.onNewGrid(grid);
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			views.blank();
		}
	}
	
	private void checkIfPolygonIsValid(Polygon polygon) throws Exception {
		if (!polygon.isValid()) {
			throw new Exception("Invalid Polygon");			
		}
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		onCameraSelected(text);
	}

	private void onCameraSelected(String text) {
		CameraInfo cameraInfo;
		try {
			cameraInfo = avaliableCameras.openFile(text);
		} catch (Exception e) {
			Toast.makeText(context,
					context.getString(R.string.error_when_opening_file),
					Toast.LENGTH_SHORT).show();
			cameraInfo = CameraInfoReader.getNewMockCameraInfo();
		}
		surveyData.setCameraInfo(cameraInfo);
		update();
	}

	private void update() {
		views.updateSeekBarsValues(surveyData);
		generateGrid();
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.checkBoxInnerWPs:
			surveyData.setInnerWpsState(views.innerWPsCheckbox.isChecked());
			update();
			break;
		case R.id.clearPolyButton:
			onSurveyListner.onClearPolygon();
			break;
		case R.id.CheckBoxFootprints:
			update();
			break;
		}
		
	}

	public pathModes getPathToDraw() {
		if (views.modeButton.isChecked()) {
			return pathModes.POLYGON;
		}else{
			return pathModes.MISSION;
		}
	}

	public  SurveyData getSurveyData() {
		return surveyData;
	}

	public boolean isFootPrintOverlayEnabled() {
		return views.footprintCheckBox.isChecked();
	}

}
