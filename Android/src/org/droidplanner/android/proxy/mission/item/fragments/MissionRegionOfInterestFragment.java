package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.RegionOfInterest;

import android.os.Bundle;
import android.view.View;

public class MissionRegionOfInterestFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_roi;
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.ROI));

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
                .getApplicationContext(), R.layout.wheel_text_centered, MIN_ALTITUDE,
                MAX_ALTITUDE, "%d m");
        CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addChangingListener(this);

        altitudePicker.setCurrentValue((int) ((RegionOfInterest) getMissionItems().get(0))
                .getCoordinate().getAltitude().valueInMeters());
    }

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
		switch (wheel.getId()) {
		case R.id.altitudePicker:
            for(MissionItem missionItem: getMissionItems()) {
                ((RegionOfInterest) missionItem).setAltitude(new Altitude(newValue));
            }
			break;
		}
	}
}
