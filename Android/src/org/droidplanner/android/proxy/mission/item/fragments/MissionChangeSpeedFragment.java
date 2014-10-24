package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.ChangeSpeed;

import android.os.Bundle;
import android.view.View;

public class MissionChangeSpeedFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_change_speed;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CHANGE_SPEED));

		ChangeSpeed item = (ChangeSpeed) getMissionItems().get(0);
		
		final NumericWheelAdapter adapter = new NumericWheelAdapter(getActivity()
				.getApplicationContext(), R.layout.wheel_text_centered, 1,
                20, "%d m/s");
		final CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addChangingListener(this);
		cardAltitudePicker.setCurrentValue((int) item.getSpeed().valueInMetersPerSecond());
	}

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
		switch (wheel.getId()) {
		case R.id.picker1:
            for(MissionItem missionItem : getMissionItems()) {
            	ChangeSpeed item = (ChangeSpeed) missionItem;
                item.setSpeed(new Speed(newValue));
            }
			break;
		}
	}
}
