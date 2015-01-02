package org.droidplanner.android.proxy.mission.item.fragments;

import android.view.View;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

public class MissionRegionOfInterestFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_roi;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

		final View view = getView();
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.REGION_OF_INTEREST));

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
				.getApplicationContext(), R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE,
				"%d m");
		CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.altitudePicker);
		altitudePicker.setViewAdapter(altitudeAdapter);
		altitudePicker.addScrollListener(this);

		altitudePicker.setCurrentValue((int) ((RegionOfInterest) getMissionItems().get(0))
				.getCoordinate().getAltitude());
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
				((RegionOfInterest) missionItem).getCoordinate().setAltitude(endValue);
			}
			break;
		}
	}
}
