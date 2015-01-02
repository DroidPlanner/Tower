package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.view.View;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.command.CameraTrigger;

public class MissionCameraTriggerFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_camera_trigger;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

        final View view = getView();
		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CAMERA_TRIGGER));

		CameraTrigger item = (CameraTrigger) getMissionItems().get(0);
		
		final NumericWheelAdapter adapter = new NumericWheelAdapter(getActivity()
				.getApplicationContext(), R.layout.wheel_text_centered, 0,
                100, "%d m");
		final CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.picker1);
		cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addScrollListener(this);
		cardAltitudePicker.setCurrentValue((int) item.getTriggerDistance());
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
            for(MissionItem missionItem : getMissionItems()) {
            	CameraTrigger item = (CameraTrigger) missionItem;
                item.setTriggerDistance(endValue);
            }
			break;
		}
	}
}
