package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.CameraTrigger;

import android.os.Bundle;
import android.view.View;

public class MissionCameraTriggerFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_camera_trigger;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CAMERA_TRIGGER));

		CameraTrigger item = (CameraTrigger) getMissionItems().get(0);

		final NumericWheelAdapter adapter = new NumericWheelAdapter(getActivity()
				.getApplicationContext(), R.layout.wheel_text_centered, 0, 100, "%d m");
		final CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		cardAltitudePicker.setViewAdapter(adapter);
		cardAltitudePicker.addChangingListener(this);
		cardAltitudePicker.setCurrentValue((int) item.getTriggerDistance().valueInMeters());
	}

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
		switch (wheel.getId()) {
		case R.id.picker1:
			for (MissionItem missionItem : getMissionItems()) {
				CameraTrigger item = (CameraTrigger) missionItem;
				item.setTriggerDistance(new Length(newValue));
			}
			break;
		}
	}
}
