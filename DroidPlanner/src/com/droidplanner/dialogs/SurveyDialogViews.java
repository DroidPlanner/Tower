package com.droidplanner.dialogs;

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
	private SurveyDialog surveyDialog;

	public SurveyDialogViews(SurveyDialog surveyDialog, Context context) {
		this.surveyDialog = surveyDialog;
		this.context = context;
	}

	void updateViews() {
		footprintTextView.setText(context.getString(string.footprint) + ": "
				+ ((Double) surveyDialog.surveyData.getLateralFootPrint()).intValue() + "x"
				+ ((Double) surveyDialog.surveyData.getLongitudinalFootPrint()).intValue()
				+ " m");
		groundResolutionTextView.setText(String.format("%s:%2.2f cm\u00B2/px",
				context.getString(string.ground_resolution),
				surveyDialog.surveyData.getGroundResolution()));
		distanceTextView
				.setText(context.getString(string.distance_between_pictures)
						+ ": "
						+ surveyDialog.surveyData.getLongitudinalPictureDistance()
								.intValue() + " m");
		distanceBetweenLinesTextView.setText(context
				.getString(string.distance_between_lines)
				+ ": "
				+ surveyDialog.surveyData.getLateralPictureDistance().intValue() + " m");
		areaTextView.setText(context.getString(string.area) + ": "
				+ surveyDialog.polygon.getArea().intValue() + " m\u00B2");
		lengthView.setText(context.getString(string.mission_length) + ": "
				+(int) surveyDialog.grid.getLength()+ " m");
		numberOfPicturesView.setText(context.getString(string.pictures) + ": "
				+(int)(surveyDialog.grid.getLength()/surveyDialog.surveyData.getLongitudinalPictureDistance()));
		numberOfStripsView.setText(context.getString(string.number_of_strips) + ": "
				+surveyDialog.grid.getNumberOfLines());
	}

	void updateSeekBarsValues(SurveyDialog surveyDialog) {
		angleView.setValue(surveyDialog.surveyData.getAngle());
		altitudeView.setValue(surveyDialog.surveyData.getAltitude());
		sidelapView.setValue(surveyDialog.surveyData.getSidelap());
		overlapView.setValue(surveyDialog.surveyData.getOverlap());
	}

	AlertDialog buildDialog() {
		Builder builder = new Builder(context);
		builder.setTitle("Survey");
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_survey, null);
		builder.setView(layout);
		builder.setNegativeButton("Cancel", surveyDialog).setPositiveButton("Ok", surveyDialog);
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
		distanceTextView = (TextView) layout
				.findViewById(id.distanceTextView);
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
		return dialog;
	}

	void updateCameraSpinner(SpinnerAdapter spinnerAdapter) {
		cameraSpinner.setAdapter(spinnerAdapter);
		cameraSpinner.setSelection(0);
	}
}