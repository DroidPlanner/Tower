package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.android.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

public class MissionWaypointFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_waypoint;
	}

    @Override
    public void onApiConnected(){
        super.onApiConnected();

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.WAYPOINT));

        final NumericWheelAdapter delayAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 60, "%d s");
        CardWheelHorizontalView delayPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .waypointDelayPicker);
        delayPicker.setViewAdapter(delayAdapter);
        delayPicker.addScrollListener(this);


        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, MIN_ALTITUDE,	MAX_ALTITUDE, "%d m");
        CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addScrollListener(this);

        final Waypoint item = (Waypoint) getMissionItems().get(0);
        delayPicker.setCurrentValue((int) item.getDelay());
        altitudePicker.setCurrentValue((int) item.getCoordinate().getAltitude());
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
            for(MissionItem item: getMissionItems()) {
                ((Waypoint)item).getCoordinate().setAltitude(endValue);
            }
			break;

		case R.id.waypointDelayPicker:
            for(MissionItem item: getMissionItems()) {
                ((Waypoint)item).setDelay(endValue);
            }
			break;
		}

	}
}
