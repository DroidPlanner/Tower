package org.droidplanner.fragments.mission.survey;

import org.droidplanner.drone.variables.mission.survey.SurveyData;
import org.droidplanner.drone.variables.mission.survey.grid.Grid;
import org.droidplanner.drone.variables.mission.survey.grid.GridBuilder;
import org.droidplanner.file.IO.CameraInfo;
import org.droidplanner.file.IO.CameraInfoReader;
import org.droidplanner.file.help.CameraInfoLoader;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.polygon.Polygon;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.droidplanner.R;
import com.google.android.gms.maps.model.LatLng;

public class SurveyFragment extends Fragment implements
		OnTextSeekBarChangedListner, OnSpinnerItemSelectedListener, OnClickListener {

	public interface OnNewGridListner{
		public void onNewGrid(Grid grid);
		public void onClearPolygon();
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
		surveyData = new SurveyData();
		views = new SurveyViews(context, surveyData);
		views.build(inflater, container,this);		
		avaliableCameras = new CameraInfoLoader(context);
		views.updateCameraSpinner(avaliableCameras.getCameraInfoList());
		
		return views.getLayout();
	}

	public void setSurveyData(Polygon polygon, Altitude defaultAltitude){
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
		surveyData.update(views.angleView.getValue(), new Altitude(views.altitudeView.getValue()),
				views.overlapView.getValue(), views.sidelapView.getValue());
		//TODO find a better origin point than (0,0)
		
		try {
			GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLng(0, 0),context);
			polygon.checkIfValid(context);
			grid = gridBuilder.generate();
			views.updateViews(grid,polygon.getArea());
			grid.setAltitude(surveyData.getAltitude());
			onSurveyListner.onNewGrid(grid);
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			views.blank();
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
		views.updateSeekBarsValues();
		generateGrid();
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.checkBoxInnerWPs:
			surveyData.setInnerWpsState(views.innerWPsCheckbox.isChecked());
			update();
			break;
		case R.id.CheckBoxFootprints:
			update();
			break;
		}		
	}

	public  SurveyData getSurveyData() {
		return surveyData;
	}

	public boolean isFootPrintOverlayEnabled() {
		return views.footprintCheckBox.isChecked();
	}

}
