package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.Circle;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MissionCircleFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener, CompoundButton.OnCheckedChangeListener {

	private static final String EXTRA_IS_ADVANCED_ON = "is_advanced_on";
	private static final boolean DEFAULT_IS_ADVANCED_ON = false;

	private CheckBox checkBoxAdvanced;

	private Circle mItem;

	private CardWheelHorizontalView mNumberStepsPicker;
	private CardWheelHorizontalView mAltitudeStepPicker;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_circle;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Context context = getActivity().getApplicationContext();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CIRCLE));

		mItem = (Circle) this.itemProxy.getMissionItem();

		final NumericWheelAdapter altitudeStepAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d m");
		mAltitudeStepPicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudeStepPicker);
		mAltitudeStepPicker.setViewAdapter(altitudeStepAdapter);
		mAltitudeStepPicker.setCurrentValue((int) mItem.getAltitudeStep());
		mAltitudeStepPicker.addChangingListener(this);

		final NumericWheelAdapter numberStepsAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d");
		mNumberStepsPicker = (CardWheelHorizontalView) view.findViewById(R.id.numberStepsPicker);
		mNumberStepsPicker.setViewAdapter(numberStepsAdapter);
		mNumberStepsPicker.setCurrentValue(mItem.getNumberOfSteps());
		mNumberStepsPicker.addChangingListener(this);

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context, MIN_ALTITUDE,
				MAX_ALTITUDE, "%d m");
		altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.altitudePicker);
		altitudePicker.setViewAdapter(altitudeAdapter);
		altitudePicker.setCurrentValue((int) mItem.getCoordinate().getAltitude().valueInMeters());
		altitudePicker.addChangingListener(this);

		final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 10, "%d");
		final CardWheelHorizontalView loiterTurnPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterTurnPicker);
		loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
		loiterTurnPicker.setCurrentValue(mItem.getNumberOfTurns());
		loiterTurnPicker.addChangingListener(this);

		final NumericWheelAdapter loiterRadiusAdapter = new NumericWheelAdapter(context, 0, 50,
				"%d m");
		loiterRadiusAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView loiterRadiusPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterRadiusPicker);
		loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
		loiterRadiusPicker.setCurrentValue((int) mItem.getRadius());
		loiterRadiusPicker.addChangingListener(this);

		boolean isAdvanced = DEFAULT_IS_ADVANCED_ON;
		if (savedInstanceState != null) {
			isAdvanced = savedInstanceState
					.getBoolean(EXTRA_IS_ADVANCED_ON, DEFAULT_IS_ADVANCED_ON);
		}
		checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxAdvanced);
		checkBoxAdvanced.setOnCheckedChangeListener(this);
		checkBoxAdvanced.setChecked(isAdvanced);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(EXTRA_IS_ADVANCED_ON,
				checkBoxAdvanced != null && checkBoxAdvanced.isChecked());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == checkBoxAdvanced) {
			int visibility;
			if (isChecked) {
				visibility = View.VISIBLE;
				mItem.setNumberOfSteps(mNumberStepsPicker.getCurrentValue());
				mItem.setAltitudeStep(mAltitudeStepPicker.getCurrentValue());
			} else {
				visibility = View.GONE;
				mItem.setNumberOfSteps(1);
			}

			mAltitudeStepPicker.setVisibility(visibility);
			mNumberStepsPicker.setVisibility(visibility);
		}
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.altitudePicker:
			mItem.setAltitude(new Altitude(newValue));
			break;

		case R.id.loiterRadiusPicker:
			mItem.setRadius(newValue);
			mItem.getMission().notifyMissionUpdate();
			break;

		case R.id.loiterTurnPicker:
			mItem.setTurns(newValue);
			break;

		case R.id.numberStepsPicker:
			if (checkBoxAdvanced.isChecked()) {
				mItem.setNumberOfSteps(newValue);
			}
			break;

		case R.id.altitudeStepPicker:
			if (checkBoxAdvanced.isChecked()) {
				mItem.setAltitudeStep(newValue);
			}
			break;
		}
	}
}
