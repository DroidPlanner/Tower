package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final Context context = getActivity().getApplicationContext();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SPLINE_WAYPOINT));

		SplineWaypoint item = (SplineWaypoint) this.itemProxy.getMissionItem();

		final NumericWheelAdapter delayAdapter = new NumericWheelAdapter(context, 0, 60, "%d s");
		delayAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView delayPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.waypointDelayPicker);
		delayPicker.setViewAdapter(delayAdapter);
		delayPicker.setCurrentValue((int) item.getDelay());
		delayPicker.addChangingListener(this);

		final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(context, MIN_ALTITUDE,
				MAX_ALTITUDE, "%d m");
		altitudeAdapter.setItemResource(R.layout.wheel_text_centered);
		final CardWheelHorizontalView altitudePicker = (CardWheelHorizontalView) view
				.findViewById(R.id.altitudePicker);
		altitudePicker.setViewAdapter(altitudeAdapter);
		altitudePicker.setCurrentValue((int) item.getCoordinate().getAltitude().valueInMeters());
		altitudePicker.addChangingListener(this);
	}

	@Override
	public void onChanged(CardWheelHorizontalView wheel, int oldValue, int newValue) {
		final SplineWaypoint item = (SplineWaypoint) this.itemProxy.getMissionItem();

		switch (wheel.getId()) {
		case R.id.altitudePicker:
			item.setAltitude(new Altitude(newValue));
			break;

		case R.id.waypointDelayPicker:
			item.setDelay(newValue);
			break;
		}

	}
}
