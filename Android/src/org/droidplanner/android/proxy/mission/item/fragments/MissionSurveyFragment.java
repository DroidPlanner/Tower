package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.proxy.mission.item.adapters.CamerasAdapter;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.CameraInfo;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.model.Drone;

import java.util.List;

public class MissionSurveyFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener,
		SpinnerSelfSelect.OnSpinnerItemSelectedListener, DroneInterfaces.OnDroneListener {

	private static final String TAG = MissionSurveyFragment.class.getSimpleName();

	private CardWheelHorizontalView mOverlapPicker;
	private CardWheelHorizontalView mAnglePicker;
	private CardWheelHorizontalView mAltitudePicker;
	private CardWheelHorizontalView mSidelapPicker;

	public TextView distanceBetweenLinesTextView;
	public TextView areaTextView;
	public TextView distanceTextView;
	public TextView footprintTextView;
	public TextView groundResolutionTextView;
	public SpinnerSelfSelect cameraSpinner;
	public TextView numberOfPicturesView;
	public TextView numberOfStripsView;
	public TextView lengthView;
	private CamerasAdapter cameraAdapter;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_survey;
	}

	@Override
	protected List<Survey> getMissionItems() {
		return (List<Survey>) super.getMissionItems();
	}

	@Override
	public void onApiConnected(DroidPlannerApi api) {
		super.onApiConnected(api);

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        cameraAdapter = new CamerasAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);

        cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);
        cameraAdapter.setTitle(getMissionItems().get(0).surveyData.getCameraName());
        
		mAnglePicker = (CardWheelHorizontalView) view.findViewById(id.anglePicker);
		mAnglePicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered,
				0, 180, "%dº"));

        mOverlapPicker = (CardWheelHorizontalView) view.findViewById(id.overlapPicker);
        mOverlapPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 99, "%d %%"));

        mSidelapPicker = (CardWheelHorizontalView) view.findViewById(R.id.sidelapPicker);
        mSidelapPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 99, "%d %%"));

        mAltitudePicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudePicker);
        mAltitudePicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 5, 200, "%d m"));

		areaTextView = (TextView) view.findViewById(id.areaTextView);
		distanceBetweenLinesTextView = (TextView) view
				.findViewById(id.distanceBetweenLinesTextView);
		footprintTextView = (TextView) view.findViewById(id.footprintTextView);
		groundResolutionTextView = (TextView) view.findViewById(id.groundResolutionTextView);
		distanceTextView = (TextView) view.findViewById(id.distanceTextView);
		numberOfPicturesView = (TextView) view.findViewById(id.numberOfPicturesTextView);
		numberOfStripsView = (TextView) view.findViewById(id.numberOfStripsTextView);
		lengthView = (TextView) view.findViewById(id.lengthTextView);

		updateViews();
		
        mAnglePicker.addChangingListener(this);
        mOverlapPicker.addChangingListener(this);
        mSidelapPicker.addChangingListener(this);
        mAltitudePicker.addChangingListener(this);

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY));

		api.addDroneListener(this);
	}

	@Override
	public void onApiDisconnected() {
		super.onApiDisconnected();
		getDroneApi().removeDroneListener(this);
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position) {
		if (spinner.getId() == id.cameraFileSpinner) {
			CameraInfo cameraInfo = cameraAdapter.getCamera(position);
			cameraAdapter.setTitle(cameraInfo.name);
			for (Survey survey : getMissionItems()) {
				survey.setCameraInfo(cameraInfo);
			}

			onChanged(mAnglePicker, 0, 0);
	        getMissionProxy().getMission().notifyMissionUpdate();
		}
	}
	
	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.anglePicker:
		case R.id.altitudePicker:
		case R.id.overlapPicker:
		case R.id.sidelapPicker:
			try {
				for (Survey survey : getMissionItems()) {
					survey.update(mAnglePicker.getCurrentValue(),
							new Altitude(mAltitudePicker.getCurrentValue()),
							mOverlapPicker.getCurrentValue(), mSidelapPicker.getCurrentValue());

					survey.build();
				}
				mAltitudePicker.setBackgroundResource(R.drawable.bg_cell_white);
			} catch (Exception e) {
				Log.e(TAG, "Error while building the survey.", e);
				mAltitudePicker.setBackgroundColor(Color.RED);
			}
			break;
		}
	}
	
	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
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
	}

	private void updateSeekBars() {
		List<Survey> surveyList = getMissionItems();
		if (!surveyList.isEmpty()) {
			Survey survey = surveyList.get(0);
			mAnglePicker.setCurrentValue(survey.surveyData.getAngle().intValue());
			mOverlapPicker.setCurrentValue((int) survey.surveyData.getOverlap());
			mSidelapPicker.setCurrentValue((int) survey.surveyData.getSidelap());
			mAltitudePicker.setCurrentValue((int) survey.surveyData.getAltitude().valueInMeters());
		}
	}
	
	private void updateTextViews() {
		boolean setDefault = true;
		List<Survey> surveyList = getMissionItems();
		if (!surveyList.isEmpty()) {
			Survey survey = surveyList.get(0);
			Context context = getActivity();
			try {
				footprintTextView.setText(getString(R.string.footprint) + ": "
						+ survey.surveyData.getLateralFootPrint() + " x"
						+ survey.surveyData.getLongitudinalFootPrint());
				groundResolutionTextView.setText(getString(R.string.ground_resolution) + ": "
						+ survey.surveyData.getGroundResolution() + "/px");
				distanceTextView.setText(getString(R.string.distance_between_pictures) + ": "
						+ survey.surveyData.getLongitudinalPictureDistance());
				distanceBetweenLinesTextView.setText(getString(R.string.distance_between_lines)
						+ ": " + survey.surveyData.getLateralPictureDistance());
				areaTextView.setText(getString(R.string.area) + ": " + survey.polygon.getArea());
				lengthView.setText(getString(R.string.mission_length) + ": "
						+ survey.grid.getLength());
				numberOfPicturesView.setText(getString(R.string.pictures) + ": "
						+ survey.grid.getCameraCount());
				numberOfStripsView.setText(getString(R.string.number_of_strips) + ": "
						+ survey.grid.getNumberOfLines());

				setDefault = false;
			} catch (Exception e) {
				setDefault = true;
			}
		}

		if (setDefault) {
			footprintTextView.setText(getString(R.string.footprint) + ": ???");
			groundResolutionTextView.setText(getString(R.string.ground_resolution) + ": ???");
			distanceTextView.setText(getString(R.string.distance_between_pictures) + ": ???");
			distanceBetweenLinesTextView.setText(getString(R.string.distance_between_lines)
					+ ": ???");
			areaTextView.setText(getString(R.string.area) + ": ???");
			lengthView.setText(getString(R.string.mission_length) + ": ???");
			numberOfPicturesView.setText(getString(R.string.pictures) + "???");
			numberOfStripsView.setText(getString(R.string.number_of_strips) + "???");
		}
	}

}
