package org.droidplanner.fragments.mission;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.variables.mission.survey.Survey;
import org.droidplanner.fragments.mission.survey.CamerasAdapter;
import org.droidplanner.helpers.units.Altitude;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListener;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class MissionSurveyFragment extends MissionDetailFragment implements
		OnClickListener, OnTextSeekBarChangedListener,
		OnSpinnerItemSelectedListener, OnDroneListener {

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
	public CheckBox footprintCheckBox;
	private CamerasAdapter cameraAdapter;

	private Survey survey;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_survey;
	}

	@Override
	public void onStart() {
		super.onStart();
		mission.addMissionUpdatesListner(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		mission.removeMissionUpdatesListner(this);
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		this.survey = ((Survey) item);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemTypes.SURVEY));
		setupLocalViews(view);
		updateViews();
	}

	public void setupLocalViews(View view) {
		cameraSpinner = (SpinnerSelfSelect) view
				.findViewById(id.cameraFileSpinner);
		footprintCheckBox = (CheckBox) view.findViewById(id.CheckBoxFootprints);

		angleView = (SeekBarWithText) view.findViewById(id.angleView);
		overlapView = (SeekBarWithText) view.findViewById(id.overlapView);
		sidelapView = (SeekBarWithText) view.findViewById(id.sidelapView);
		altitudeView = (SeekBarWithText) view.findViewById(id.altitudeView);

		innerWPsCheckbox = (CheckBox) view.findViewById(id.checkBoxInnerWPs);

		areaTextView = (TextView) view.findViewById(id.areaTextView);
		distanceBetweenLinesTextView = (TextView) view
				.findViewById(id.distanceBetweenLinesTextView);
		footprintTextView = (TextView) view.findViewById(id.footprintTextView);
		groundResolutionTextView = (TextView) view
				.findViewById(id.groundResolutionTextView);
		distanceTextView = (TextView) view.findViewById(id.distanceTextView);
		numberOfPicturesView = (TextView) view
				.findViewById(id.numberOfPicturesTextView);
		numberOfStripsView = (TextView) view
				.findViewById(id.numberOfStripsTextView);
		lengthView = (TextView) view.findViewById(id.lengthTextView);

		cameraAdapter = new CamerasAdapter(getActivity(),
				android.R.layout.simple_spinner_dropdown_item);
		cameraSpinner.setAdapter(cameraAdapter);

		footprintCheckBox.setOnClickListener(this);
		angleView.setOnChangedListener(this);
		altitudeView.setOnChangedListener(this);
		overlapView.setOnChangedListener(this);
		sidelapView.setOnChangedListener(this);
		innerWPsCheckbox.setOnClickListener(this);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);

	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		survey.setCameraInfo(cameraAdapter.getCamera(position));
	}

	@Override
	public void onSeekBarChanged() {
		survey.update(angleView.getValue(),
				new Altitude(altitudeView.getValue()), overlapView.getValue(),
				sidelapView.getValue());
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_UPDATE:
			updateViews();
			break;
		default:
			break;
		}

	}

	private void updateViews() {
		updateTextViews();			
		updateSeekBars();
		updateCameraSpinner();
	}

	private void updateCameraSpinner() {
		cameraAdapter.setTitle(survey.surveyData.getCameraName());
	}

	private void updateSeekBars() {
		altitudeView.setValue(survey.surveyData.getAltitude().valueInMeters());
		angleView.setValue(survey.surveyData.getAngle());
		overlapView.setValue(survey.surveyData.getOverlap());
		sidelapView.setValue(survey.surveyData.getSidelap());
	}

	private void updateTextViews() {
		Context context = getActivity();
		try{
			footprintTextView.setText(context.getString(R.string.footprint) + ": "
					+ survey.surveyData.getLateralFootPrint() + " x"
					+ survey.surveyData.getLongitudinalFootPrint());
			groundResolutionTextView.setText(context
					.getString(R.string.ground_resolution)
					+ ": "
					+ survey.surveyData.getGroundResolution() + "/px");
			distanceTextView.setText(context
					.getString(R.string.distance_between_pictures)
					+ ": "
					+ survey.surveyData.getLongitudinalPictureDistance());
			distanceBetweenLinesTextView.setText(context
					.getString(R.string.distance_between_lines)
					+ ": "
					+ survey.surveyData.getLateralPictureDistance());
			areaTextView.setText(context.getString(R.string.area) + ": "
					+ survey.polygon.getArea());
			lengthView.setText(context.getString(R.string.mission_length) + ": "
					+ survey.grid.getLength());
			numberOfPicturesView.setText(context.getString(R.string.pictures)
					+ ": " + survey.grid.getCameraCount());
			numberOfStripsView.setText(context.getString(R.string.number_of_strips)
					+ ": " + survey.grid.getNumberOfLines());
		}catch (Exception e){
			footprintTextView.setText(context.getString(R.string.footprint) + ": "
					+ "???");
			groundResolutionTextView.setText(context
					.getString(R.string.ground_resolution)
					+ ": "+ "???");
			distanceTextView.setText(context
					.getString(R.string.distance_between_pictures)
					+ ": "+ "???");
			distanceBetweenLinesTextView.setText(context
					.getString(R.string.distance_between_lines)
					+ ": "+ "???");
			areaTextView.setText(context.getString(R.string.area) + ": "
					+ "???");
			lengthView.setText(context.getString(R.string.mission_length) + ": "
					+ "???");
			numberOfPicturesView.setText(context.getString(R.string.pictures)
					+ "???");
			numberOfStripsView.setText(context.getString(R.string.number_of_strips)
					+ "???");
		}
	}
}
