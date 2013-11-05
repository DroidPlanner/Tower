package com.droidplanner.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnTuningDataListner;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartSeries;

public class TuningFragment extends Fragment implements OnTuningDataListner {

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

	private ChartSeries bottomDataReference;

	private ChartSeries bottomDataValue;

	private ChartSeries topDataReference;

	private ChartSeries topDataValue;

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
		topDataReference = new ChartSeries(800);
		topDataReference.setColor(Color.BLUE);
		topDataReference.enable();
		topChart.series.add(topDataReference);
		topDataValue = new ChartSeries(800);
		topDataValue.setColor(Color.WHITE);
		topDataValue.enable();
		topChart.series.add(topDataValue);
		
		bottomDataReference = new ChartSeries(800);
		bottomDataReference.setColor(Color.BLUE);
		bottomDataReference.enable();
		bottomChart.series.add(bottomDataReference);
		bottomDataValue = new ChartSeries(800);
		bottomDataValue.setColor(Color.WHITE);
		bottomDataValue.enable();
		bottomChart.series.add(bottomDataValue);
		
	}

	@Override
	public void onNewOrientationData() {
		 bottomDataValue.newData(drone.orientation.getPitch());
		 bottomChart.update();
		 
		 topDataValue.newData(drone.orientation.getRoll());		 
		 topChart.update();
	}

	@Override
	public void onNewNavigationData() {
		 bottomDataReference.newData(drone.navigation.getNavPitch());
		 bottomChart.update();
		 
		 topDataReference.newData(drone.navigation.getNavRoll());		 
		 topChart.update();		
	}

}
