package com.droidplanner.fragments.survey;

import java.util.List;

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
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.file.IO.CameraInfoReader;
import com.droidplanner.file.help.CameraInfoLoader;
import com.droidplanner.helpers.units.Area;
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
		public void onNewGrid(List<waypoint> grid);
	}
	
	private Context context;
	private Polygon polygon;

	private SurveyData surveyData;
	private CameraInfoLoader avaliableCameras;
	private SurveyDialogViews views;
	private Grid grid;
	
	private OnNewGridListner onNewGridListner;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity().getApplicationContext();
		views = new SurveyDialogViews(context);
		views.build(inflater, container,this);
		
		//TODO find better values for the bellow
		double defaultHatchAngle = 0;
		double defaultAltitude = 0;
		surveyData = new SurveyData(Math.floor(defaultHatchAngle), defaultAltitude);
		avaliableCameras = new CameraInfoLoader(context);
		views.updateCameraSpinner(avaliableCameras.getCameraInfoList());
		
		return views.getLayout();
	}

	public void setSurveyData(Polygon polygon){
		this.polygon = polygon;		
	}
	
	public void setOnNewGridListner(OnNewGridListner listner){
		this.onNewGridListner = listner;
	}

	
	@Override
	public void onSeekBarChanged() {
		
		surveyData.update(views.angleView.getValue(), views.altitudeView.getValue(),
				views.overlapView.getValue(), views.sidelapView.getValue());
		//TODO find a better origin point than (0,0)
		
		try {
			GridBuilder gridBuilder = new GridBuilder(polygon, surveyData, new LatLng(0, 0));
			checkIfPolygonIsValid(polygon);
			grid = gridBuilder.generate();
			//views.updateViews(surveyData,grid,polygon.getArea());
			views.updateViews(surveyData,grid,new Area(0.0)); // TODO use correct area value
			
			onNewGridListner.onNewGrid(grid.getWaypoints(surveyData.getAltitude()));
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
		views.updateSeekBarsValues(surveyData);
		onSeekBarChanged();
	}
	
	@Override
	public void onClick(View view) {
		if (view.equals(views.innerWPsCheckbox)) {
			surveyData.setInnerWpsState(views.innerWPsCheckbox.isChecked());
		}
		
	}

}
