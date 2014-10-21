package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.CylindricalSurvey;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MissionCylindricalSurveyFragment extends MissionDetailFragment
		implements CardWheelHorizontalView.OnCardWheelChangedListener,
		CompoundButton.OnCheckedChangeListener {

	private CheckBox checkBoxAdvanced;
	private CardWheelHorizontalView radiusPicker, startAltitudeStepPicker,
			endAltitudeStepPicker, mNumberStepsPicker;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_cylindrical_mapping;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Context context = getActivity().getApplicationContext();

		Log.d("DEBUG", "onViewCreated");
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemType.CYLINDRICAL_SURVEY));

		// Use the first one as reference.
		final CylindricalSurvey firstItem = ((List<CylindricalSurvey>) getMissionItems())
				.get(0);

		radiusPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.radiusPicker);
		radiusPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 2, 50, "%d m"));
		radiusPicker.addChangingListener(this);
		radiusPicker.setCurrentValue((int) firstItem.getRadius()
				.valueInMeters());

		startAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.startAltitudePicker);
		startAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE,
				"%d m"));
		startAltitudeStepPicker.addChangingListener(this);
		startAltitudeStepPicker.setCurrentValue((int) firstItem
				.getStartAltitude().valueInMeters());

		endAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.heightStepPicker);
		endAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE,
				"%d m"));
		endAltitudeStepPicker.addChangingListener(this);
		endAltitudeStepPicker.setCurrentValue((int) firstItem.getEndAltitude()
				.valueInMeters());

		mNumberStepsPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.stepsPicker);
		mNumberStepsPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d"));
		mNumberStepsPicker.addChangingListener(this);
		mNumberStepsPicker.setCurrentValue(firstItem.getNumberOfSteps());

		checkBoxAdvanced = (CheckBox) view
				.findViewById(R.id.checkBoxSurveyCrossHatch);
		checkBoxAdvanced.setOnCheckedChangeListener(this);
		checkBoxAdvanced.setChecked(firstItem.isCrossHatchEnabled());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		getItem().enableCrossHatch(isChecked);
        getMissionProxy().getMission().notifyMissionUpdate();
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue,
			int newValue) {
		switch (cardWheel.getId()) {
		case R.id.radiusPicker:
			getItem().setRadius(newValue);
            getMissionProxy().getMission().notifyMissionUpdate();
			break;
		case R.id.startAltitudePicker:
			getItem().setStartAltitude(newValue);
            getMissionProxy().getMission().notifyMissionUpdate();
			break;
		case R.id.heightStepPicker:
			getItem().setAltitudeStep(newValue);
			break;
		case R.id.stepsPicker:
			getItem().setNumberOfSteps(newValue);
			break;
		}
	}

	private CylindricalSurvey getItem() {
		CylindricalSurvey cylindricalSurvey = (CylindricalSurvey) getMissionItems()
				.get(0);
		return cylindricalSurvey;
	}
}
