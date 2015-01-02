package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.view.View;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;

public class MissionChangeSpeedFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_change_speed;
	}

    @Override
    public void onApiConnected(){
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CHANGE_SPEED));

        final NumericWheelAdapter adapter = new NumericWheelAdapter(getActivity()
                .getApplicationContext(), R.layout.wheel_text_centered, 1,
                20, "%d m/s");
        CardWheelHorizontalView cardAltitudePicker = (CardWheelHorizontalView) view.findViewById
                (R.id.picker1);
        cardAltitudePicker.setViewAdapter(adapter);
        cardAltitudePicker.addScrollListener(this);

        ChangeSpeed item = (ChangeSpeed) getMissionItems().get(0);
        cardAltitudePicker.setCurrentValue((int) item.getSpeed());
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
            	ChangeSpeed item = (ChangeSpeed) missionItem;
                item.setSpeed(endValue);
            }
			break;
		}
	}
}
