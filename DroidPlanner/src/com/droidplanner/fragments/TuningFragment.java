package com.droidplanner.fragments;

import com.droidplanner.R;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartSeries;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TuningFragment extends Fragment {

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tunning, container,
				false);
		setupLocalViews(view);		
		setupCharts();
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
		ChartSeries topDataReference = new ChartSeries(800);
		topDataReference.setColor(Color.WHITE);
		topDataReference.enable();
		topChart.series.add(topDataReference);
		ChartSeries topDataValue = new ChartSeries(800);
		topDataValue.setColor(Color.BLUE);
		topDataValue.enable();
		topChart.series.add(topDataValue);
		
		ChartSeries bottomDataReference = new ChartSeries(800);
		bottomDataReference.setColor(Color.WHITE);
		bottomDataReference.enable();
		bottomChart.series.add(bottomDataReference);
		ChartSeries bottomDataValue = new ChartSeries(800);
		bottomDataValue.setColor(Color.BLUE);
		bottomDataValue.enable();
		bottomChart.series.add(bottomDataValue);
		
	}

}
