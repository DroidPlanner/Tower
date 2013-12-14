package com.droidplanner.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnTuningDataListner;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.series.DynamicSeries;

public class TuningFragment extends Fragment implements OnTuningDataListner {

	private static final int NAV_MSG_RATE = 50;
	private static final int CHART_BUFFER_SIZE = 20*NAV_MSG_RATE; // About 20s of data on the buffer


	private Drone drone;
	
	private Chart topChart;
	private Chart bottomChart;
	
	private SeekBarWithText rollPSeekBar;
	private SeekBarWithText rollDSeekBar;
	private SeekBarWithText yawPSeekBar;
	private SeekBarWithText thrAclSeekBar;

	private Parameter rollP;
	private Parameter rollD;
	private Parameter yawP;
	private Parameter thrAcl;

	private DynamicSeries bottomDataReference;

	private DynamicSeries bottomDataValue;

	private DynamicSeries topDataReference;

	private DynamicSeries topDataValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tunning, container,
				false);
		setupLocalViews(view);		
		setupCharts();

		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		drone.setTuningDataListner(this);
		
		return view;	
	}

	@Override
	public void onStart() {
		super.onStart();
		setupDataStreamingForTuning(); 
	}

	private void setupDataStreamingForTuning() {
		// Sets the nav messages at 50Hz and other messages at a low rate 1Hz
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1, 0, 0, NAV_MSG_RATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		MavLinkStreamRates.setupStreamRatesFromPref((DroidPlannerApp) getActivity().getApplication());
	}

	private void setupLocalViews(View view) {
		topChart = (Chart) view.findViewById(R.id.chartTop);
		bottomChart = (Chart) view.findViewById(R.id.chartBottom);
		
		rollPSeekBar = (SeekBarWithText) view
				.findViewById(R.id.SeekBarRollPitchControl);
		rollDSeekBar = (SeekBarWithText) view
				.findViewById(R.id.SeekBarRollPitchDampenning);
		yawPSeekBar = (SeekBarWithText) view
				.findViewById(R.id.SeekBarYawControl);
		thrAclSeekBar = (SeekBarWithText) view
				.findViewById(R.id.SeekBarThrottleAccel);
	}

	private void setupCharts() {
		topDataReference = new DynamicSeries(800);
		topDataReference.setColor(Color.BLUE);
		topDataReference.enable();
		topChart.series.add(topDataReference);
		topDataValue = new DynamicSeries(800);
		topDataValue.setColor(Color.WHITE);
		topDataValue.enable();
		topChart.series.add(topDataValue);
		
		bottomDataReference = new DynamicSeries(CHART_BUFFER_SIZE);
		bottomDataReference.setColor(Color.BLUE);
		bottomDataReference.enable();
		bottomChart.series.add(bottomDataReference);
		bottomDataValue = new DynamicSeries(CHART_BUFFER_SIZE);
		bottomDataValue.setColor(Color.WHITE);
		bottomDataValue.enable();
		bottomChart.series.add(bottomDataValue);
		
	}

	@Override
	public void onNewOrientationData() {
		 bottomDataValue.newData(drone.orientation.getPitch());
		 topDataValue.newData(drone.orientation.getRoll());		 
		 bottomDataReference.newData(drone.navigation.getNavPitch());		 
		 topDataReference.newData(drone.navigation.getNavRoll());	
		 bottomChart.update();
		 topChart.update();
	}


	@Override
	public void onNewNavigationData() {
	}
	
}
