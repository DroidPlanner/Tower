package com.droidplanner.dialogs.survey;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.R.id;
import com.droidplanner.R.string;
import com.droidplanner.helpers.units.Area;
import com.droidplanner.helpers.units.Length;
import com.droidplanner.survey.SurveyData;
import com.droidplanner.survey.grid.Grid;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public class SurveyDialogViews {
	public Context context;
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

	public SurveyDialogViews(Context context) {
		this.context = context;
	}

	void updateViews(SurveyData surveyData, Grid grid, Area area) {
		footprintTextView.setText(context.getString(string.footprint) + ": "
				+ surveyData.getLateralFootPrint() + " x"
				+ surveyData.getLongitudinalFootPrint());
		groundResolutionTextView.setText(context.getString(string.ground_resolution)+surveyData.getGroundResolution()+"/px");
		distanceTextView
				.setText(context.getString(string.distance_between_pictures)
						+ ": "
						+ surveyData.getLongitudinalPictureDistance());
		distanceBetweenLinesTextView.setText(context
				.getString(string.distance_between_lines)
				+ ": "
				+ surveyData.getLateralPictureDistance());
		areaTextView.setText(context.getString(string.area) + ": "
				+ area);
		lengthView.setText(context.getString(string.mission_length) + ": "
				+ grid.getLength());
		numberOfPicturesView.setText(context.getString(string.pictures)
				+ ": "
				+ (int) (grid.getLength().valueInMeters() / surveyData
						.getLongitudinalPictureDistance().valueInMeters()));
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

	void updateSeekBarsValues(SurveyData surveyData) {
		angleView.setValue(surveyData.getAngle());
		altitudeView.setValue(surveyData.getAltitude());
		sidelapView.setValue(surveyData.getSidelap());
		overlapView.setValue(surveyData.getOverlap());
	}

	AlertDialog buildDialog(SurveyDialog surveyDialog) {
		Builder builder = new Builder(context);
		builder.setTitle("Survey");
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_survey, null);
		builder.setView(layout);
		builder.setNegativeButton("Cancel", surveyDialog).setPositiveButton(
				"Ok", surveyDialog);
		AlertDialog dialog = builder.create();

		cameraSpinner = (SpinnerSelfSelect) layout
				.findViewById(id.cameraFileSpinner);
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

		angleView.setOnChangedListner(surveyDialog);
		altitudeView.setOnChangedListner(surveyDialog);
		overlapView.setOnChangedListner(surveyDialog);
		sidelapView.setOnChangedListner(surveyDialog);
		innerWPsCheckbox.setOnClickListener(surveyDialog);
		cameraSpinner.setOnSpinnerItemSelectedListener(surveyDialog);
		return dialog;
	}

	void updateCameraSpinner(SpinnerAdapter spinnerAdapter) {
		cameraSpinner.setAdapter(spinnerAdapter);
		cameraSpinner.setSelection(0);
	}
}