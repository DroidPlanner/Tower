package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.widgets.graph.Chart;
import org.droidplanner.android.widgets.graph.ChartSeries;
import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This fragment is used to tune the roll and pitch of the drone.
 */
public class TuningFragment extends Fragment implements OnDroneListener {

	private static final int NAV_MSG_RATE = 50;
	private static final int CHART_BUFFER_SIZE = 20 * NAV_MSG_RATE; // About 20s
																	// of data
																	// on the
																	// buffer

	private Drone drone;

	private Chart topChart;
	private Chart bottomChart;

	/*
	 * private Parameter rollP; private Parameter rollD; private Parameter yawP;
	 * private Parameter thrAcl;
	 */

	private ChartSeries bottomDataReference;

	private ChartSeries bottomDataValue;

	private ChartSeries topDataReference;

	private ChartSeries topDataValue;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tuning, container, false);

		setupLocalViews(view);
		setupCharts();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.addDroneListener(this);
		setupDataStreamingForTuning();
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.removeDroneListener(this);
		drone.getStreamRates().setupStreamRatesFromPref();
	}

	private void setupDataStreamingForTuning() {
		// Sets the nav messages at 50Hz and other messages at a low rate 1Hz
		MavLinkStreamRates
				.setupStreamRates(drone.getMavClient(), 1, 0, 1, 1, 1, 0, 0, NAV_MSG_RATE);
	}

	private void setupLocalViews(View view) {
		topChart = (Chart) view.findViewById(R.id.chartTop);
		bottomChart = (Chart) view.findViewById(R.id.chartBottom);

		/*
		 * SeekBarWithText rollPSeekBar = (SeekBarWithText) view
		 * .findViewById(R.id.SeekBarRollPitchControl); SeekBarWithText
		 * rollDSeekBar = (SeekBarWithText) view
		 * .findViewById(R.id.SeekBarRollPitchDampenning); SeekBarWithText
		 * yawPSeekBar = (SeekBarWithText)
		 * view.findViewById(R.id.SeekBarYawControl); SeekBarWithText
		 * thrAclSeekBar = (SeekBarWithText) view
		 * .findViewById(R.id.SeekBarThrottleAccel);
		 */
	}

	private void setupCharts() {
		topDataReference = new ChartSeries(800);
		topDataReference.setColor(Color.BLUE);
		topDataReference.enable();
		topChart.series.add(topDataReference);
		topDataValue = new ChartSeries(800);
		topDataValue.setColor(Color.WHITE);
		topDataValue.enable();
		topChart.series.add(topDataValue);

		bottomDataReference = new ChartSeries(CHART_BUFFER_SIZE);
		bottomDataReference.setColor(Color.BLUE);
		bottomDataReference.enable();
		bottomChart.series.add(bottomDataReference);
		bottomDataValue = new ChartSeries(CHART_BUFFER_SIZE);
		bottomDataValue.setColor(Color.WHITE);
		bottomDataValue.enable();
		bottomChart.series.add(bottomDataValue);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case ORIENTATION:
			onNewOrientationData(drone);
			break;

		default:
			break;
		}

	}

	public void onNewOrientationData(Drone drone) {
		bottomDataValue.newData(drone.getOrientation().getPitch());
		topDataValue.newData(drone.getOrientation().getRoll());
		bottomDataReference.newData(drone.getNavigation().getNavPitch());
		topDataReference.newData(drone.getNavigation().getNavRoll());
		bottomChart.update();
		topChart.update();
	}
}
