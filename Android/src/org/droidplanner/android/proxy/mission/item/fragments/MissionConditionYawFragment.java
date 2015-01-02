package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.command.YawCondition;

public class MissionConditionYawFragment extends MissionDetailFragment
		implements CardWheelHorizontalView.OnCardWheelScrollListener,
		OnCheckedChangeListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_condition_yaw;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

        final View  view = getView();
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.YAW_CONDITION));

		YawCondition item = (YawCondition) getMissionItems().get(0);

		final NumericWheelAdapter adapter = new NumericWheelAdapter(
				getActivity().getApplicationContext(),
				R.layout.wheel_text_centered, 0, 359, "%d deg");
		final CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		cardAltitudePicker.setViewAdapter(adapter);
		cardAltitudePicker.addScrollListener(this);
		cardAltitudePicker.setCurrentValue((int) item.getAngle());

		CheckBox checkBoxRelative = (CheckBox) view.findViewById(R.id.checkBox1);
		checkBoxRelative.setOnCheckedChangeListener(this);
		checkBoxRelative.setChecked(item.isRelative());
	}

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, int startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {

    }

    @Override
	public void onScrollingEnded(CardWheelHorizontalView wheel, int startValue, int endValue) {
		switch (wheel.getId()) {
		case R.id.picker1:
			for (MissionItem missionItem : getMissionItems()) {
				YawCondition item = (YawCondition) missionItem;
				item.setAngle(endValue);
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.checkBox1) {
			for (MissionItem missionItem : getMissionItems()) {
				((YawCondition) missionItem).setRelative(isChecked);
			}
		}
	}
}
