package com.droidplanner.widgets.spinners;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.MAVLink.Drone;
import com.MAVLink.Messages.ApmModes;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

public class SelectModeSpinner extends SpinnerSelfSelect implements OnSpinnerItemSelectedListener {
	public interface OnModeSpinnerSelectedListener {
		void OnModeSpinnerSelected(String Mode);
	}

	private OnModeSpinnerSelectedListener listener;
	
	private ArrayList<String> modeSpinnerAdapter;

	public SelectModeSpinner(Context context) {
		super(context);
	}
	
	public void buildSpinner(Context context, OnModeSpinnerSelectedListener listener ){
		modeSpinnerAdapter = new ArrayList<String>();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item,
				modeSpinnerAdapter);
		
		updateModeSpinner(null);
		setAdapter(adapter);
		setOnSpinnerItemSelectedListener(this);
		setOnWaypointSpinnerSelectedListener(listener);
	}

	public void updateModeSpinner(Drone drone) {
		modeSpinnerAdapter.clear();
		if (drone != null) {
				updateWpSpinnerWithList(drone);
		}
		updateWpSpinnerWithNoData();
		return;
	}

	private void updateWpSpinnerWithNoData() {
		modeSpinnerAdapter.add("Mode");
	}

	private void updateWpSpinnerWithList(Drone drone) {
		modeSpinnerAdapter.clear();
		modeSpinnerAdapter.addAll(ApmModes.getModeList(drone.type));
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		listener.OnModeSpinnerSelected(text);
	}
	
	
	public void setOnWaypointSpinnerSelectedListener(
			OnModeSpinnerSelectedListener listener) {
		this.listener = listener;
	}
}
