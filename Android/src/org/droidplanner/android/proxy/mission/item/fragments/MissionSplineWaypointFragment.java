package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;

/**
 * This class renders the detail view for a spline waypoint mission item.
 */
public class MissionSplineWaypointFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_spline_waypoint;
	}

    @Override
    public void onApiConnected(){
        super.onApiConnected();

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SPLINE_WAYPOINT));

        final NumericWheelAdapter delayAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 60, "%d s");
        CardWheelHorizontalView delayPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .waypointDelayPicker);
        delayPicker.setViewAdapter(delayAdapter);
        delayPicker.addChangingListener(this);

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, MIN_ALTITUDE,	MAX_ALTITUDE, "%d m");
        CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view.findViewById(R.id
                .altitudePicker);
        altitudePicker.setViewAdapter(altitudeAdapter);
        altitudePicker.addChangingListener(this);

        SplineWaypoint item = (SplineWaypoint) getMissionItems().get(0);
        delayPicker.setCurrentValue((int) item.getDelay());
        altitudePicker.setCurrentValue((int) item.getCoordinate().getAltitude());
    }

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
		switch (wheel.getId()) {
		case R.id.altitudePicker:
            for(MissionItem item: getMissionItems()) {
                ((SplineWaypoint)item).getCoordinate().setAltitude(newValue);
            }
			break;

		case R.id.waypointDelayPicker:
            for(MissionItem item: getMissionItems()) {
                ((SplineWaypoint)item).setDelay(newValue);
            }
			break;
		}
	}
}
