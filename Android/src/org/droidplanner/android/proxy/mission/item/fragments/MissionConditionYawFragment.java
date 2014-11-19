package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.ConditionYaw;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MissionConditionYawFragment extends MissionDetailFragment
		implements CardWheelHorizontalView.OnCardWheelChangedListener,
		OnCheckedChangeListener {

	private CheckBox checkBoxRelative;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_condition_yaw;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemType.CONDITION_YAW));

		ConditionYaw item = (ConditionYaw) getMissionItems().get(0);

		final NumericWheelAdapter adapter = new NumericWheelAdapter(
				getActivity().getApplicationContext(),
				R.layout.wheel_text_centered, 0, 359, "%d deg");
		final CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		cardAltitudePicker.setViewAdapter(adapter);
		cardAltitudePicker.addChangingListener(this);
		cardAltitudePicker.setCurrentValue((int) item.getAngle());

		checkBoxRelative = (CheckBox) view.findViewById(R.id.checkBox1);
		checkBoxRelative.setOnCheckedChangeListener(this);
		checkBoxRelative.setChecked(item.isRelative());
	}

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue,
			int newValue) {
		switch (wheel.getId()) {
		case R.id.picker1:
			for (MissionItem missionItem : getMissionItems()) {
				ConditionYaw item = (ConditionYaw) missionItem;
				item.setAngle(newValue);
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.checkBox1) {
			for (MissionItem missionItem : getMissionItems()) {
				((ConditionYaw) missionItem).setRelative(isChecked);
			}

		}
	}
}
