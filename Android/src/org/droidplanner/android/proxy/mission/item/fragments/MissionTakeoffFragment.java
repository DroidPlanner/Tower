package org.droidplanner.android.proxy.mission.item.fragments;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class MissionTakeoffFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_takeoff;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.TAKEOFF));

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
				.getApplicationContext(), R.layout.wheel_text_centered, 0, MAX_ALTITUDE, "%d m");
		CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) getView()
				.findViewById(R.id.altitudePicker);
		cardAltitudePicker.setViewAdapter(altitudeAdapter);
		cardAltitudePicker.addScrollListener(this);

		Takeoff item = (Takeoff) getMissionItems().get(0);
		cardAltitudePicker.setCurrentValue((int) item.getTakeoffAltitude());
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
		case R.id.altitudePicker:
			for (MissionItem missionItem : getMissionItems()) {
				Takeoff item = (Takeoff) missionItem;
				item.setTakeoffAltitude(endValue);
			}
			break;
		}
	}
}
