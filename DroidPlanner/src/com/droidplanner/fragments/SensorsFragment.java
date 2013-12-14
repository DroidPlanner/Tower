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
import com.droidplanner.drone.DroneInterfaces.OnSensorDataListner;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.series.ChartSeries;
import com.droidplanner.widgets.graph.series.DynamicSeries;

public class SensorsFragment extends Fragment implements OnSensorDataListner {

	private static final int NAV_MSG_RATE = 50;
	private static final int CHART_BUFFER_SIZE = 64; 


	private Drone drone;
	
	private Chart topChart;
	private Chart bottomChart;

	private DynamicSeries dataX;
	private DynamicSeries dataY;
	private DynamicSeries dataZ;
	private ChartSeries dataFFT;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sensors, container,
				false);
		setupLocalViews(view);		
		setupCharts();

		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		drone.setSensorsDatalistner(this);
		
		return view;	
	}

	@Override
	public void onStart() {
		super.onStart();
		setupDataStreamingForIMUdata(); 
	}

	private void setupDataStreamingForIMUdata() {
		// Sets the IMU messages at 50Hz and other messages at a low rate 1Hz
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1, 0,
				NAV_MSG_RATE, 0);
	}

	@Override
	public void onStop() {
		super.onStop();
		MavLinkStreamRates.setupStreamRatesFromPref((DroidPlannerApp) getActivity().getApplication());
	}

	private void setupLocalViews(View view) {
		topChart = (Chart) view.findViewById(R.id.chartTop);
		bottomChart = (Chart) view.findViewById(R.id.chartBottom);
	}

	private void setupCharts() {
		dataX = new DynamicSeries(800);
		dataX.setColor(Color.BLUE);
		dataX.enable();
		topChart.series.add(dataX);
		dataY = new DynamicSeries(800);
		dataY.setColor(Color.RED);
		dataY.enable();
		topChart.series.add(dataY);
		dataZ = new DynamicSeries(CHART_BUFFER_SIZE);
		dataZ.setColor(Color.GREEN);
		dataZ.enable();
		topChart.series.add(dataZ);
		topChart.scale.y.setRange(1.5);
		
		dataFFT = new DynamicSeries(CHART_BUFFER_SIZE);
		dataFFT.setColor(Color.WHITE);
		dataFFT.enable();
		bottomChart.series.add(dataFFT);
	}

	@Override
	public void onNewAccelData() {
		dataX.newData(drone.sensors.xacc);
		dataY.newData(drone.sensors.yacc);
		dataZ.newData(drone.sensors.zacc);
		//dataFFT.newData(drone);

		bottomChart.update();
		topChart.update();		
	}
	
}
