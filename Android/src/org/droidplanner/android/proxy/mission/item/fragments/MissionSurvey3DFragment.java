package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.android.proxy.mission.item.adapters.CamerasAdapter;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.Survey3D;
import org.droidplanner.core.survey.CameraInfo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class MissionSurvey3DFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener, CompoundButton.OnCheckedChangeListener {

	private CheckBox checkBoxAdvanced;
	private CardWheelHorizontalView startAltitudeStepPicker, stepHeightStepPicker;
	private SpinnerSelfSelect cameraSpinner;
	private CamerasAdapter cameraAdapter;
	private List<Survey3D> missionItems;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_survey3d;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Context context = getActivity().getApplicationContext();

		Log.d("DEBUG", "onViewCreated");
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY3D));

		missionItems = (List<Survey3D>) getMissionItems();
		// Use the first one as reference.
		final Survey3D firstItem = missionItems.get(0);

		cameraAdapter = new CamerasAdapter(getActivity(),
				android.R.layout.simple_spinner_dropdown_item);
		cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
		cameraSpinner.setAdapter(cameraAdapter);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);
		cameraAdapter.setTitle(firstItem.getCamera());

		startAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.startAltitudePicker);
		startAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE, "%d m"));
		startAltitudeStepPicker.addChangingListener(this);
		//startAltitudeStepPicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude().valueInMeters());

		stepHeightStepPicker = (CardWheelHorizontalView) view.findViewById(R.id.heightStepPicker);
		stepHeightStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, MAX_ALTITUDE, "%d m"));
		stepHeightStepPicker.addChangingListener(this);
		//stepHeightStepPicker.setCurrentValue((int) firstItem.getEndAltitude().valueInMeters());

		checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxSurveyCrossHatch);
		checkBoxAdvanced.setOnCheckedChangeListener(this);
		//checkBoxAdvanced.setChecked(firstItem.isCrossHatchEnabled());

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//getItem().enableCrossHatch(isChecked);
		getMissionProxy().getMission().notifyMissionUpdate();
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.startAltitudePicker:
			//getItem().setAltitude(new Altitude(newValue));
			break;
		case R.id.heightStepPicker:
			//getItem().setAltitudeStep(newValue);
			break;
		}
		getMissionProxy().getMission().notifyMissionUpdate();
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position) {
		if (spinner.getId() == id.cameraFileSpinner) {
			CameraInfo cameraInfo = cameraAdapter.getCamera(position);
			cameraAdapter.setTitle(cameraInfo.name);
			for (Survey3D scan : missionItems) {
				scan.setCameraInfo(cameraInfo);
			}
			getMissionProxy().getMission().notifyMissionUpdate();
		}
	}

	private Survey3D getItem() {
		Survey3D cylindricalSurvey = (Survey3D) getMissionItems().get(0);
		return cylindricalSurvey;
	}
}
