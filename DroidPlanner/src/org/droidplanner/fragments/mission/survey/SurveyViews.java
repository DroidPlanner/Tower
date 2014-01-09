package org.droidplanner.fragments.mission.survey;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.R.string;
import org.droidplanner.drone.variables.mission.survey.SurveyData;
import org.droidplanner.drone.variables.mission.survey.grid.Grid;
import org.droidplanner.helpers.units.Area;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SurveyViews implements OnClickListener, OnTextSeekBarChangedListner, OnSpinnerItemSelectedListener {
	public SeekBarWithText overlapView;
	public SeekBarWithText angleView;
	public SeekBarWithText altitudeView;
	public SeekBarWithText sidelapView;
	public TextView distanceBetweenLinesTextView;
	public TextView areaTextView;
	public TextView distanceTextView;
	public TextView footprintTextView;
	public TextView groundResolutionTextView;
	public SpinnerSelfSelect cameraSpinner;
	public CheckBox innerWPsCheckbox;
	public TextView numberOfPicturesView;
	public TextView numberOfStripsView;
	public TextView lengthView;
	private Context context;
	private View layout;
	public CheckBox footprintCheckBox;
	private SurveyData surveyData;

	public SurveyViews(Context context, SurveyData surveyData) {
		this.context = context;
		this.surveyData = surveyData;
	}

	void updateViews(Grid grid, Area area) {
		footprintTextView.setText(context.getString(string.footprint) + ": "
				+ surveyData.getLateralFootPrint() + " x"
				+ surveyData.getLongitudinalFootPrint());
		groundResolutionTextView.setText(context
				.getString(string.ground_resolution)
				+ surveyData.getGroundResolution() + "/px");
		distanceTextView.setText(context
				.getString(string.distance_between_pictures)
				+ ": "
				+ surveyData.getLongitudinalPictureDistance());
		distanceBetweenLinesTextView.setText(context
				.getString(string.distance_between_lines)
				+ ": "
				+ surveyData.getLateralPictureDistance());
		areaTextView.setText(context.getString(string.area) + ": " + area);
		lengthView.setText(context.getString(string.mission_length) + ": "
				+ grid.getLength());
		numberOfPicturesView.setText(context.getString(string.pictures) + ": "
				+ grid.getCameraCount());
		numberOfStripsView.setText(context.getString(string.number_of_strips)
				+ ": " + grid.getNumberOfLines());
	}

	public void blank() {
		String unknowData = "???";
		footprintTextView.setText(unknowData);
		groundResolutionTextView.setText(unknowData);
		distanceTextView.setText(unknowData);
		distanceBetweenLinesTextView.setText(unknowData);
		areaTextView.setText(unknowData);
		lengthView.setText(unknowData);
		numberOfPicturesView.setText(unknowData);
		numberOfStripsView.setText(unknowData);

	}

	void updateSeekBarsValues() {
		angleView.setValue(surveyData.getAngle());
		altitudeView.setValue(surveyData.getAltitude().valueInMeters());
		sidelapView.setValue(surveyData.getSidelap());
		overlapView.setValue(surveyData.getOverlap());
	}

	public void build(LayoutInflater inflater, ViewGroup container,
			SurveyFragment surveyDialog) {
		layout = inflater.inflate(R.layout.fragment_editor_detail_survey, null);
		cameraSpinner = (SpinnerSelfSelect) layout
				.findViewById(id.cameraFileSpinner);
		footprintCheckBox = (CheckBox) layout
				.findViewById(id.CheckBoxFootprints);

		angleView = (SeekBarWithText) layout.findViewById(id.angleView);
		overlapView = (SeekBarWithText) layout.findViewById(id.overlapView);
		sidelapView = (SeekBarWithText) layout.findViewById(id.sidelapView);
		altitudeView = (SeekBarWithText) layout.findViewById(id.altitudeView);

		innerWPsCheckbox = (CheckBox) layout.findViewById(id.checkBoxInnerWPs);

		areaTextView = (TextView) layout.findViewById(id.areaTextView);
		distanceBetweenLinesTextView = (TextView) layout
				.findViewById(id.distanceBetweenLinesTextView);
		footprintTextView = (TextView) layout
				.findViewById(id.footprintTextView);
		groundResolutionTextView = (TextView) layout
				.findViewById(id.groundResolutionTextView);
		distanceTextView = (TextView) layout.findViewById(id.distanceTextView);
		numberOfPicturesView = (TextView) layout
				.findViewById(id.numberOfPicturesTextView);
		numberOfStripsView = (TextView) layout
				.findViewById(id.numberOfStripsTextView);
		lengthView = (TextView) layout.findViewById(id.lengthTextView);

		footprintCheckBox.setOnClickListener(this);
		angleView.setOnChangedListner(this);
		altitudeView.setOnChangedListner(this);
		overlapView.setOnChangedListner(this);
		sidelapView.setOnChangedListner(this);
		innerWPsCheckbox.setOnClickListener(this);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);
	}

	void updateCameraSpinner(SpinnerAdapter spinnerAdapter) {
		cameraSpinner.setAdapter(spinnerAdapter);
		cameraSpinner.setSelection(0);
	}

	public View getLayout() {
		return layout;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.checkBoxInnerWPs:
			surveyData.setInnerWpsState(innerWPsCheckbox.isChecked());
			update();
			break;
		case R.id.CheckBoxFootprints:
			update();
			break;
		}		
	}
	
	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		//onCameraSelected(text);
	}
	

	@Override
	public void onSeekBarChanged() {					
	}
	
	/*
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
	
	 */
	private void update() {
		//views.updateSeekBarsValues();
	}

}