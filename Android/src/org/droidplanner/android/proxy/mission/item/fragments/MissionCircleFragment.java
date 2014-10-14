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

import java.util.List;

public class MissionCircleFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener, CompoundButton.OnCheckedChangeListener {

	private static final String EXTRA_IS_ADVANCED_ON = "is_advanced_on";
	private static final boolean DEFAULT_IS_ADVANCED_ON = false;

	private CheckBox checkBoxAdvanced;

	private List<Circle> mItemsList;

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

		mItemsList = (List<Circle>) getMissionItems();

        //Use the first one as reference.
        final Circle firstItem = mItemsList.get(0);

        boolean isAdvanced = DEFAULT_IS_ADVANCED_ON;
        if (savedInstanceState != null) {
            isAdvanced = savedInstanceState
                    .getBoolean(EXTRA_IS_ADVANCED_ON, DEFAULT_IS_ADVANCED_ON);
        }
        checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxAdvanced);
        checkBoxAdvanced.setOnCheckedChangeListener(this);
        checkBoxAdvanced.setChecked(isAdvanced);

		final NumericWheelAdapter altitudeStepAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d m");
		mAltitudeStepPicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudeStepPicker);
		mAltitudeStepPicker.setViewAdapter(altitudeStepAdapter);
        mAltitudeStepPicker.addChangingListener(this);
		mAltitudeStepPicker.setCurrentValue((int) firstItem.getAltitudeStep());

		final NumericWheelAdapter numberStepsAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d");
		mNumberStepsPicker = (CardWheelHorizontalView) view.findViewById(R.id.numberStepsPicker);
		mNumberStepsPicker.setViewAdapter(numberStepsAdapter);
        mNumberStepsPicker.addChangingListener(this);
		mNumberStepsPicker.setCurrentValue(firstItem.getNumberOfSteps());

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context, MIN_ALTITUDE,
				MAX_ALTITUDE, "%d m");
		altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.altitudePicker);
		altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addChangingListener(this);
		altitudePicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude().valueInMeters
                ());

		final NumericWheelAdapter loiterTurnAdapter = new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 0, 10, "%d");
		final CardWheelHorizontalView loiterTurnPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterTurnPicker);
		loiterTurnPicker.setViewAdapter(loiterTurnAdapter);
        loiterTurnPicker.addChangingListener(this);
		loiterTurnPicker.setCurrentValue(firstItem.getNumberOfTurns());

		final NumericWheelAdapter loiterRadiusAdapter = new NumericWheelAdapter(context, 0, 50,
				"%d m");
		loiterRadiusAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView loiterRadiusPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.loiterRadiusPicker);
		loiterRadiusPicker.setViewAdapter(loiterRadiusAdapter);
        loiterRadiusPicker.addChangingListener(this);
		loiterRadiusPicker.setCurrentValue((int) firstItem.getRadius());
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
                for(Circle item: mItemsList) {
                    item.setNumberOfSteps(mNumberStepsPicker.getCurrentValue());
                    item.setAltitudeStep(mAltitudeStepPicker.getCurrentValue());
                }
			} else {
				visibility = View.GONE;
                for(Circle item: mItemsList) {
                    item.setNumberOfSteps(1);
                }
			}

			mAltitudeStepPicker.setVisibility(visibility);
			mNumberStepsPicker.setVisibility(visibility);
		}
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.altitudePicker:
            for(Circle item: mItemsList) {
                item.setAltitude(new Altitude(newValue));
            }
			break;

		case R.id.loiterRadiusPicker:
            for(Circle item: mItemsList) {
                item.setRadius(newValue);
            }
            getMissionProxy().getMission().notifyMissionUpdate();
			break;

		case R.id.loiterTurnPicker:
            for(Circle item: mItemsList) {
                item.setTurns(newValue);
            }
			break;

		case R.id.numberStepsPicker:
			if (checkBoxAdvanced.isChecked()) {
                for(Circle item: mItemsList) {
                    item.setNumberOfSteps(newValue);
                }
			}
			break;

		case R.id.altitudeStepPicker:
			if (checkBoxAdvanced.isChecked()) {
                for(Circle item: mItemsList) {
                    item.setAltitudeStep(newValue);
                }
			}
			break;
		}
	}
}
