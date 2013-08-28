package com.droidplanner.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.droidplanner.R;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.file.IO.CameraInfo;
import com.droidplanner.file.IO.CameraInfoReader;
import com.droidplanner.file.help.CameraInfoLoader;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.survey.SurveyData;
import com.droidplanner.survey.grid.GridBuilder;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;
import com.google.android.gms.maps.model.LatLng;

public abstract class SurveyDialog implements DialogInterface.OnClickListener,
		OnTextSeekBarChangedListner, OnSpinnerItemSelectedListener {
	public abstract void onPolygonGenerated(List<waypoint> list);

	public Context context;
	private SeekBarWithText overlapView;
	private SeekBarWithText angleView;
	private SeekBarWithText altitudeView;
	private SeekBarWithText sidelapView;
	private TextView distanceBetweenLinesTextView;
	private TextView areaTextView;
	private TextView distanceTextView;
	private TextView footprintTextView;
	private TextView groundResolutionTextView;

	private Polygon polygon;
	private LatLng originPoint;

	public SurveyData surveyData;
	private SpinnerSelfSelect cameraSpinner;
	private CameraInfoLoader avaliableCameras;
	private CheckBox innerWPsCheckbox;

	public void generateSurveyDialog(Polygon polygon, double defaultHatchAngle,
			LatLng lastPoint, double defaultAltitude, Context context) {
		this.polygon = polygon;
		this.originPoint = lastPoint;
		this.context = context;

		avaliableCameras = new CameraInfoLoader(this.context);

		if (checkIfPolygonIsValid(polygon)) {
			Toast.makeText(context, "Invalid Polygon", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		AlertDialog dialog = buildDialog(context);

		surveyData = new SurveyData(Math.floor(defaultHatchAngle), defaultAltitude);

		cameraSpinner.setOnSpinnerItemSelectedListener(this);
		updateCameraSpinner(context);

		dialog.show();
	}

	private void updateCameraSpinner(Context context) {
		cameraSpinner.setAdapter(avaliableCameras.getCameraInfoList());
		cameraSpinner.setSelection(0);
	}

	@Override
	public void onSeekBarChanged() {
		surveyData.update(angleView.getValue(), altitudeView.getValue(),
				overlapView.getValue(), sidelapView.getValue());
		
		localGridProcessing();		
		
		updateViews();
		
	}

	private void localGridProcessing() {
		long time = SystemClock.elapsedRealtime();
		
		List<waypoint> result = buildGrid();
		Log.d("GRID", "Building the grid took (mS):"+(SystemClock.elapsedRealtime()-time));
		time = SystemClock.elapsedRealtime();
		
		double lenght = 0;
		for (int i = 1; i < result.size(); i++) {
			lenght+=GeoTools.getDistance(result.get(i).getCoord(),result.get(i-1).getCoord());
		}
		Log.d("GRID", "Measuring the total length took (mS):"+(SystemClock.elapsedRealtime()-time));
		time = SystemClock.elapsedRealtime();
		
		Log.d("GRID", "Grid Stats: \n Length "+lenght+" \nNumberOfPictures "+(lenght/surveyData.getLongitudinalPictureDistance())+"\n Legs "+(result.size()/2));
		
		
	}

	private void updateSeekBarsValues() {
		angleView.setValue(surveyData.getAngle());
		altitudeView.setValue(surveyData.getAltitude());
		sidelapView.setValue(surveyData.getSidelap());
		overlapView.setValue(surveyData.getOverlap());
	}

	private void updateViews() {
		footprintTextView.setText(context.getString(R.string.footprint) + ": "
				+ ((Double) surveyData.getLateralFootPrint()).intValue() + "x"
				+ ((Double) surveyData.getLongitudinalFootPrint()).intValue()
				+ " m");
		groundResolutionTextView.setText(String.format("%s:%2.2f cm\u00B2",
				context.getString(R.string.ground_resolution),
				surveyData.getGroundResolution()));
		distanceTextView
				.setText(context.getString(R.string.distance_between_pictures)
						+ ": "
						+ surveyData.getLongitudinalPictureDistance()
								.intValue() + " m");
		distanceBetweenLinesTextView.setText(context
				.getString(R.string.distance_between_lines)
				+ ": "
				+ surveyData.getLateralPictureDistance().intValue() + " m");
		areaTextView.setText(context.getString(R.string.area) + ": "
				+ polygon.getArea().intValue() + " m\u00B2");
	}

	private boolean checkIfPolygonIsValid(Polygon polygon) {
		return !polygon.isValid();
	}

	private AlertDialog buildDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Survey");
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_survey, null);
		builder.setView(layout);
		builder.setNegativeButton("Cancel", this).setPositiveButton("Ok", this);
		AlertDialog dialog = builder.create();

		cameraSpinner = (SpinnerSelfSelect) layout
				.findViewById(R.id.cameraFileSpinner);
		angleView = (SeekBarWithText) layout.findViewById(R.id.angleView);
		overlapView = (SeekBarWithText) layout.findViewById(R.id.overlapView);
		sidelapView = (SeekBarWithText) layout.findViewById(R.id.sidelapView);
		altitudeView = (SeekBarWithText) layout.findViewById(R.id.altitudeView);
		innerWPsCheckbox = (CheckBox) layout.findViewById(R.id.checkBoxInnerWPs);

		areaTextView = (TextView) layout.findViewById(R.id.areaTextView);
		distanceBetweenLinesTextView = (TextView) layout
				.findViewById(R.id.distanceBetweenLinesTextView);
		footprintTextView = (TextView) layout
				.findViewById(R.id.footprintTextView);
		groundResolutionTextView = (TextView) layout
				.findViewById(R.id.groundResolutionTextView);
		distanceTextView = (TextView) layout
				.findViewById(R.id.distanceTextView);

		angleView.setOnChangedListner(this);
		altitudeView.setOnChangedListner(this);
		overlapView.setOnChangedListner(this);
		sidelapView.setOnChangedListner(this);
		return dialog;
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		if (which == Dialog.BUTTON_POSITIVE) {
			List<waypoint> result = buildGrid();
			onPolygonGenerated(result);
		}
	}

	private List<waypoint> buildGrid() {
		GridBuilder grid = new GridBuilder(polygon, surveyData.getAngle(),
				surveyData.getLateralPictureDistance(), originPoint,
				surveyData.getAltitude());
		grid.setGenerateInnerWaypoints(innerWPsCheckbox.isChecked());
		List<waypoint> result = grid.generate();
		return result;
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
		updateSeekBarsValues();
		updateViews();
	}

}
