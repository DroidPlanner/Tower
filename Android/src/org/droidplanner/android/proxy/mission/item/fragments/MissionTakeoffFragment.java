package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.commands.Takeoff;

import android.os.Bundle;
import android.view.View;

public class MissionTakeoffFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_takeoff;
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.TAKEOFF));

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
                .getApplicationContext(), R.layout.wheel_text_centered, 0, MAX_ALTITUDE, "%d m");
        CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) getView()
                .findViewById(R.id.altitudePicker);
        cardAltitudePicker.setViewAdapter(altitudeAdapter);
        cardAltitudePicker.addChangingListener(this);

        Takeoff item = (Takeoff) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue((int) item.getFinishedAlt().valueInMeters());
    }

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
		switch (wheel.getId()) {
		case R.id.altitudePicker:
            for(MissionItem missionItem : getMissionItems()) {
                Takeoff item = (Takeoff) missionItem;
                item.setFinishedAlt(new Altitude(newValue));
            }
			break;
		}
	}
}
